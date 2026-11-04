using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Winx.API.Data;
using Winx.API.Models;

namespace Winx.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MediaController : ControllerBase
    {
        private const long MaxPhotoBytes = 10L * 1024 * 1024;
        private const long MaxVideoBytes = 100L * 1024 * 1024;
        private readonly AppDbContext _context;
        private readonly IWebHostEnvironment _environment;

        public MediaController(AppDbContext context, IWebHostEnvironment environment)
        {
            _context = context;
            _environment = environment;
        }

        // GET: api/Media/entry/1
        [HttpGet("entry/{travelEntryId:int}")]
        public async Task<ActionResult<IEnumerable<MediaItem>>> GetMediaForEntry(int travelEntryId)
        {
            var entryExists = await _context.TravelEntries.AnyAsync(e => e.Id == travelEntryId);
            if (!entryExists)
                return NotFound(new { message = $"Travel entry with ID {travelEntryId} was not found." });

            var media = await _context.MediaItems
                .Where(m => m.TravelEntryId == travelEntryId)
                .AsNoTracking()
                .ToListAsync();
            return Ok(media);
        }

        // GET: api/Media/1
        [HttpGet("{id:int}")]
        public async Task<ActionResult<MediaItem>> GetMedia(int id)
        {
            var media = await _context.MediaItems.AsNoTracking().FirstOrDefaultAsync(m => m.Id == id);
            if (media == null)
                return NotFound(new { message = $"Media item with ID {id} was not found." });
            return Ok(media);
        }

        // POST: api/Media
        // Retained for compatibility with clients that register existing media URLs.
        [HttpPost]
        public async Task<ActionResult<MediaItem>> CreateMedia(MediaItem media)
        {
            if (media.TravelEntryId <= 0)
                return BadRequest(new { message = "A valid travel entry ID is required." });

            var mediaType = media.MediaType?.Trim();
            if (mediaType != "Photo" && mediaType != "Video")
                return BadRequest(new { message = "Media type must be either Photo or Video." });
            if (string.IsNullOrWhiteSpace(media.FileName))
                return BadRequest(new { message = "File name is required." });
            if (string.IsNullOrWhiteSpace(media.FilePath))
                return BadRequest(new { message = "File path is required." });
            if (!await _context.TravelEntries.AnyAsync(e => e.Id == media.TravelEntryId))
                return NotFound(new { message = $"Travel entry with ID {media.TravelEntryId} was not found." });

            // Only the multipart upload endpoint may assign an idempotency key.
            media.Id = 0;
            media.UploadId = null;
            media.MediaType = mediaType;
            media.FileName = media.FileName.Trim();
            media.FilePath = media.FilePath.Trim();
            media.TravelEntry = null;

            _context.MediaItems.Add(media);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetMedia), new { id = media.Id }, media);
        }

        // GET: api/Media/upload/{uploadId}
        // Allows clients to reconcile a lost upload response before retrying.
        [HttpGet("upload/{uploadId}")]
        public async Task<ActionResult<MediaItem>> GetMediaByUploadId(string uploadId, CancellationToken cancellationToken)
        {
            if (!Guid.TryParse(uploadId, out var parsedId))
                return BadRequest(new { message = "A valid upload UUID is required." });

            var normalizedId = parsedId.ToString("D");
            var media = await _context.MediaItems.AsNoTracking()
                .FirstOrDefaultAsync(m => m.UploadId == normalizedId, cancellationToken);
            if (media == null)
                return NotFound(new { message = "No completed upload exists for this upload ID." });
            return Ok(media);
        }

        // POST: api/Media/upload
        // multipart/form-data fields: travelEntryId, file, optional uploadId (UUID).
        // Existing clients that omit uploadId continue to work without deduplication.
        [HttpPost("upload")]
        [Consumes("multipart/form-data")]
        [RequestSizeLimit(MaxVideoBytes + 1024 * 1024)]
        public async Task<ActionResult<MediaItem>> UploadMedia(
            [FromForm] int travelEntryId,
            [FromForm] IFormFile? file,
            [FromForm] string? uploadId,
            CancellationToken cancellationToken)
        {
            if (travelEntryId <= 0)
                return BadRequest(new { message = "A valid travel entry ID is required." });

            string? normalizedUploadId = null;
            if (!string.IsNullOrWhiteSpace(uploadId))
            {
                if (!Guid.TryParse(uploadId, out var parsedId))
                    return BadRequest(new { message = "uploadId must be a valid UUID." });
                normalizedUploadId = parsedId.ToString("D");

                var existing = await _context.MediaItems.AsNoTracking()
                    .FirstOrDefaultAsync(m => m.UploadId == normalizedUploadId, cancellationToken);
                if (existing != null)
                {
                    if (existing.TravelEntryId != travelEntryId)
                        return Conflict(new { message = "This upload ID belongs to another travel entry." });
                    return Ok(existing);
                }
            }

            if (file == null || file.Length == 0)
                return BadRequest(new { message = "A non-empty photo or video file is required." });
            if (!await _context.TravelEntries.AnyAsync(e => e.Id == travelEntryId, cancellationToken))
                return NotFound(new { message = $"Travel entry with ID {travelEntryId} was not found." });

            var extension = Path.GetExtension(file.FileName).ToLowerInvariant();
            var mediaType = extension switch
            {
                ".jpg" or ".jpeg" or ".png" or ".gif" or ".webp" => "Photo",
                ".mp4" or ".mov" or ".webm" => "Video",
                _ => null
            };
            if (mediaType == null)
                return BadRequest(new { message = "Supported formats: JPG, PNG, GIF, WEBP, MP4, MOV, WEBM." });

            var limit = mediaType == "Photo" ? MaxPhotoBytes : MaxVideoBytes;
            if (file.Length > limit)
                return BadRequest(new { message = $"{mediaType} exceeds the permitted size limit." });

            await using (var input = file.OpenReadStream())
            {
                var header = new byte[16];
                var bytesRead = await input.ReadAsync(header.AsMemory(), cancellationToken);
                if (!MatchesSignature(extension, header, bytesRead))
                    return BadRequest(new { message = "The file content does not match its extension." });
            }

            var webRoot = _environment.WebRootPath;
            if (string.IsNullOrWhiteSpace(webRoot))
                webRoot = Path.Combine(_environment.ContentRootPath, "wwwroot");

            var uploadDirectory = Path.Combine(webRoot, "uploads");
            Directory.CreateDirectory(uploadDirectory);
            var storedName = $"{Guid.NewGuid():N}{extension}";
            var storedPath = Path.Combine(uploadDirectory, storedName);
            var relativeUrl = $"/uploads/{storedName}";

            var fileCommitted = false;
            try
            {
                await using (var output = new FileStream(storedPath, FileMode.CreateNew, FileAccess.Write, FileShare.None))
                {
                    await file.CopyToAsync(output, cancellationToken);
                }

                var media = new MediaItem
                {
                    TravelEntryId = travelEntryId,
                    UploadId = normalizedUploadId,
                    MediaType = mediaType,
                    FileName = Path.GetFileName(file.FileName),
                    FilePath = $"{Request.Scheme}://{Request.Host}{Request.PathBase}{relativeUrl}"
                };
                _context.MediaItems.Add(media);

                try
                {
                    await _context.SaveChangesAsync(cancellationToken);
                    fileCommitted = true;
                    return CreatedAtAction(nameof(GetMedia), new { id = media.Id }, media);
                }
                catch (DbUpdateException) when (normalizedUploadId != null)
                {
                    // Another request may have committed the same UUID concurrently.
                    // A unique database index is required for this to be reliable.
                    _context.Entry(media).State = EntityState.Detached;
                    var existing = await _context.MediaItems.AsNoTracking()
                        .FirstOrDefaultAsync(m => m.UploadId == normalizedUploadId, cancellationToken);
                    if (existing == null) throw;
                    if (existing.TravelEntryId != travelEntryId)
                        return Conflict(new { message = "This upload ID belongs to another travel entry." });
                    return Ok(existing);
                }
            }
            finally
            {
                // Remove the new file if the request failed or another request
                // already committed the same upload ID.
                if (!fileCommitted && System.IO.File.Exists(storedPath))
                {
                    try { System.IO.File.Delete(storedPath); }
                    catch (IOException) { }
                    catch (UnauthorizedAccessException) { }
                }
            }
        }

        // DELETE: api/Media/1
        [HttpDelete("{id:int}")]
        public async Task<IActionResult> DeleteMedia(int id)
        {
            var media = await _context.MediaItems.FirstOrDefaultAsync(m => m.Id == id);
            if (media == null)
                return NotFound(new { message = $"Media item with ID {id} was not found." });

            var storedFile = GetManagedUploadPath(media.FilePath);
            _context.MediaItems.Remove(media);
            await _context.SaveChangesAsync();

            if (storedFile != null && System.IO.File.Exists(storedFile))
            {
                try { System.IO.File.Delete(storedFile); }
                catch (IOException) { }
                catch (UnauthorizedAccessException) { }
            }
            return NoContent();
        }

        private string? GetManagedUploadPath(string? filePath)
        {
            if (string.IsNullOrWhiteSpace(filePath)) return null;
            if (!Uri.TryCreate(filePath, UriKind.Absolute, out var uri)) return null;
            if (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps) return null;
            if (!string.Equals(uri.Host, Request.Host.Host, StringComparison.OrdinalIgnoreCase)) return null;
            if (uri.Port != Request.Host.Port.GetValueOrDefault(uri.Scheme == "https" ? 443 : 80)) return null;

            var path = uri.AbsolutePath;
            var prefix = Request.PathBase.Value?.TrimEnd('/') + "/uploads/";
            if (string.IsNullOrEmpty(prefix)) prefix = "/uploads/";
            if (!path.StartsWith(prefix, StringComparison.Ordinal)) return null;
            var fileName = path.Substring(prefix.Length);
            if (string.IsNullOrWhiteSpace(fileName) || fileName != Path.GetFileName(fileName)) return null;
            var root = _environment.WebRootPath;
            if (string.IsNullOrWhiteSpace(root)) root = Path.Combine(_environment.ContentRootPath, "wwwroot");
            return Path.Combine(root, "uploads", fileName);
        }

        private static bool MatchesSignature(string extension, byte[] data, int length)
        {
            bool Starts(params byte[] bytes) => length >= bytes.Length && bytes.Select((value, index) => data[index] == value).All(match => match);
            bool At(int offset, string ascii) => length >= offset + ascii.Length &&
                System.Text.Encoding.ASCII.GetString(data, offset, ascii.Length) == ascii;
            return extension switch
            {
                ".jpg" or ".jpeg" => Starts(0xFF, 0xD8, 0xFF),
                ".png" => Starts(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
                ".gif" => At(0, "GIF87a") || At(0, "GIF89a"),
                ".webp" => At(0, "RIFF") && At(8, "WEBP"),
                ".mp4" or ".mov" => At(4, "ftyp"),
                ".webm" => Starts(0x1A, 0x45, 0xDF, 0xA3),
                _ => false
            };
        }
    }
}
