using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Winx.API.Data;
using Winx.API.Models;

namespace Winx.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class EntriesController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IWebHostEnvironment _environment;
    private readonly ILogger<EntriesController> _logger;

    public EntriesController(
        AppDbContext context,
        IWebHostEnvironment environment,
        ILogger<EntriesController> logger)
    {
        _context = context;
        _environment = environment;
        _logger = logger;
    }

    // GET: api/Entries
    [HttpGet]
    public async Task<ActionResult<IEnumerable<TravelEntry>>> GetEntries()
    {
        var entries = await _context.TravelEntries.AsNoTracking().ToListAsync();
        return Ok(entries);
    }

    // GET: api/Entries/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<TravelEntry>> GetEntry(int id)
    {
        var entry = await _context.TravelEntries.AsNoTracking()
            .FirstOrDefaultAsync(e => e.Id == id);

        if (entry == null)
            return NotFound(new { message = $"Entry with ID {id} was not found." });

        return Ok(entry);
    }

    // GET: api/Entries/by-client-id/{clientId}
    [HttpGet("by-client-id/{clientId:guid}")]
    public async Task<ActionResult<TravelEntry>> GetEntryByClientId(Guid clientId)
    {
        var entry = await _context.TravelEntries.AsNoTracking()
            .FirstOrDefaultAsync(e => e.ClientId == clientId);

        if (entry == null)
            return NotFound(new { message = "No entry was found for this client ID." });

        return Ok(entry);
    }

    // POST: api/Entries
    [HttpPost]
    public async Task<ActionResult<TravelEntry>> CreateEntry(TravelEntry entry)
    {
        if (entry.Rating < 1 || entry.Rating > 5)
            return BadRequest(new { message = "Rating must be between 1 and 5." });

        if (entry.ClientId.HasValue)
        {
            var existingEntry = await _context.TravelEntries.AsNoTracking()
                .FirstOrDefaultAsync(e => e.ClientId == entry.ClientId);
            if (existingEntry != null)
                return Ok(existingEntry);
        }

        entry.Id = 0;
        _context.TravelEntries.Add(entry);

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException) when (entry.ClientId.HasValue)
        {
            // A concurrent upload may already have created the same entry.
            var existingEntry = await _context.TravelEntries.AsNoTracking()
                .FirstOrDefaultAsync(e => e.ClientId == entry.ClientId);
            if (existingEntry != null)
                return Ok(existingEntry);
            throw;
        }

        return CreatedAtAction(nameof(GetEntry), new { id = entry.Id }, entry);
    }

    // PUT: api/Entries/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateEntry(int id, TravelEntry updatedEntry)
    {
        if (id != updatedEntry.Id && updatedEntry.Id != 0)
            return BadRequest(new
            {
                message = "The entry ID in the URL does not match the entry ID in the request."
            });

        if (updatedEntry.Rating < 1 || updatedEntry.Rating > 5)
            return BadRequest(new { message = "Rating must be between 1 and 5." });

        var existingEntry = await _context.TravelEntries.FirstOrDefaultAsync(e => e.Id == id);
        if (existingEntry == null)
            return NotFound(new { message = $"Entry with ID {id} was not found." });

        existingEntry.Title = updatedEntry.Title;
        existingEntry.Location = updatedEntry.Location;
        existingEntry.Country = updatedEntry.Country;
        existingEntry.Date = updatedEntry.Date;
        existingEntry.Rating = updatedEntry.Rating;
        existingEntry.Notes = updatedEntry.Notes;

        await _context.SaveChangesAsync();
        return NoContent();
    }

    // DELETE: api/Entries/5
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteEntry(int id)
    {
        var entry = await _context.TravelEntries.FirstOrDefaultAsync(e => e.Id == id);
        if (entry == null)
            return NotFound(new { message = $"Entry with ID {id} was not found." });

        // Capture paths before EF Core cascades deletion of related media rows.
        var mediaPaths = await _context.MediaItems.AsNoTracking()
            .Where(m => m.TravelEntryId == id)
            .Select(m => m.FilePath)
            .ToListAsync();

        _context.TravelEntries.Remove(entry);
        await _context.SaveChangesAsync();

        // Never remove a file until the database deletion has succeeded.
        // Only delete server-managed uploads; external URLs and local device paths are ignored.
        foreach (var filePath in mediaPaths)
        {
            var managedPath = GetManagedUploadPath(filePath);
            if (managedPath == null)
                continue;

            try
            {
                if (System.IO.File.Exists(managedPath))
                    System.IO.File.Delete(managedPath);
            }
            catch (IOException exception)
            {
                _logger.LogWarning(exception, "Could not remove uploaded media file {FilePath}", managedPath);
            }
            catch (UnauthorizedAccessException exception)
            {
                _logger.LogWarning(exception, "Access denied removing uploaded media file {FilePath}", managedPath);
            }
        }

        return NoContent();
    }

    // Match the managed-file URL convention used by MediaController.
    // Only accept a URL for this server's /uploads/{filename} directory.
    private string? GetManagedUploadPath(string? filePath)
    {
        if (string.IsNullOrWhiteSpace(filePath) ||
            !Uri.TryCreate(filePath, UriKind.Absolute, out var uri) ||
            (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps))
            return null;

        if (!string.Equals(uri.Host, Request.Host.Host, StringComparison.OrdinalIgnoreCase))
            return null;

        var expectedPort = Request.Host.Port.GetValueOrDefault(
            uri.Scheme == Uri.UriSchemeHttps ? 443 : 80);
        if (uri.Port != expectedPort)
            return null;

        var prefix = (Request.PathBase.Value?.TrimEnd('/') ?? "") + "/uploads/";
        if (!uri.AbsolutePath.StartsWith(prefix, StringComparison.Ordinal))
            return null;

        var fileName = uri.AbsolutePath.Substring(prefix.Length);
        if (string.IsNullOrWhiteSpace(fileName) ||
            fileName != Path.GetFileName(fileName) ||
            fileName == "." || fileName == ".." ||
            fileName.Contains('%') || fileName.Contains('\\'))
            return null;

        var webRoot = _environment.WebRootPath;
        if (string.IsNullOrWhiteSpace(webRoot))
            webRoot = Path.Combine(_environment.ContentRootPath, "wwwroot");

        return Path.Combine(webRoot, "uploads", fileName);
    }
}
