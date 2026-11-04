namespace Winx.API.Models
{
    public class MediaItem
    {
        public int Id { get; set; }

        public int TravelEntryId { get; set; }

        public string MediaType { get; set; } = string.Empty;

        public string FileName { get; set; } = string.Empty;

        public string FilePath { get; set; } = string.Empty;

        // Stable client-generated UUID for retry-safe photo/video uploads.
        // Nullable so existing media records and legacy create requests remain valid.
        public string? UploadId { get; set; }

        public TravelEntry? TravelEntry { get; set; }
    }
}
