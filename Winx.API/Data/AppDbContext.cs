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

            modelBuilder.Entity<MediaItem>()
                .ToTable("MediaItem");

            modelBuilder.Entity<MediaItem>()
                .HasOne(m => m.TravelEntry)
                .WithMany(t => t.MediaItems)
                .HasForeignKey(m => m.TravelEntryId)
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
