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
        private readonly AppDbContext _context;

        public MediaController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/Media/entry/1
        [HttpGet("entry/{travelEntryId}")]
        public async Task<ActionResult<IEnumerable<MediaItem>>> GetMediaForEntry(int travelEntryId)
        {
            var entryExists = await _context.TravelEntries
                .AnyAsync(e => e.Id == travelEntryId);

            if (!entryExists)
            {
                return NotFound(new
                {
                    message = $"Travel entry with ID {travelEntryId} was not found."
                });
            }

            var media = await _context.MediaItems
                .Where(m => m.TravelEntryId == travelEntryId)
                .AsNoTracking()
                .ToListAsync();

            return Ok(media);
        }

        // GET: api/Media/1
        [HttpGet("{id}")]
        public async Task<ActionResult<MediaItem>> GetMedia(int id)
        {
            var media = await _context.MediaItems
                .AsNoTracking()
                .FirstOrDefaultAsync(m => m.Id == id);

            if (media == null)
            {
                return NotFound(new
                {
                    message = $"Media item with ID {id} was not found."
                });
            }

            return Ok(media);
        }

        // POST: api/Media
        [HttpPost]
        public async Task<ActionResult<MediaItem>> CreateMedia(MediaItem media)
        {
            if (media.TravelEntryId <= 0)
            {
                return BadRequest(new
                {
                    message = "A valid travel entry ID is required."
                });
            }

            if (string.IsNullOrWhiteSpace(media.MediaType))
            {
                return BadRequest(new
                {
                    message = "Media type is required."
                });
            }

            if (media.MediaType != "Photo" && media.MediaType != "Video")
            {
                return BadRequest(new
                {
                    message = "Media type must be either Photo or Video."
                });
            }

            if (string.IsNullOrWhiteSpace(media.FileName))
            {
                return BadRequest(new
                {
                    message = "File name is required."
                });
            }

            if (string.IsNullOrWhiteSpace(media.FilePath))
            {
                return BadRequest(new
                {
                    message = "File path is required."
                });
            }

            var entryExists = await _context.TravelEntries
                .AnyAsync(e => e.Id == media.TravelEntryId);

            if (!entryExists)
            {
                return NotFound(new
                {
                    message = $"Travel entry with ID {media.TravelEntryId} was not found."
                });
            }

            media.Id = 0;
            media.MediaType = media.MediaType.Trim();
            media.FileName = media.FileName.Trim();
            media.FilePath = media.FilePath.Trim();

            _context.MediaItems.Add(media);
            await _context.SaveChangesAsync();

            return CreatedAtAction(
                nameof(GetMedia),
                new { id = media.Id },
                media);
        }

        // DELETE: api/Media/1
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteMedia(int id)
        {
            var media = await _context.MediaItems
                .FirstOrDefaultAsync(m => m.Id == id);

            if (media == null)
            {
                return NotFound(new
                {
                    message = $"Media item with ID {id} was not found."
                });
            }

            _context.MediaItems.Remove(media);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}
