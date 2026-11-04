using Microsoft.EntityFrameworkCore;
using Winx.API.Models;

namespace Winx.API.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options)
            : base(options)
        {
        }

        public DbSet<TravelEntry> TravelEntries { get; set; }

        public DbSet<MediaItem> MediaItems { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // SQLite permits multiple NULL values in a unique index, so older
            // travel entries without a ClientId remain supported.
            modelBuilder.Entity<TravelEntry>()
                .HasIndex(e => e.ClientId)
                .IsUnique();

            modelBuilder.Entity<MediaItem>()
                .ToTable("MediaItem");

            // A stable upload ID prevents duplicate media records when an
            // offline upload is retried after a lost server response.
            // Existing media without an UploadId remain supported.
            modelBuilder.Entity<MediaItem>()
                .HasIndex(m => m.UploadId)
                .IsUnique();

            modelBuilder.Entity<MediaItem>()
                .HasOne(m => m.TravelEntry)
                .WithMany(t => t.MediaItems)
                .HasForeignKey(m => m.TravelEntryId)
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
