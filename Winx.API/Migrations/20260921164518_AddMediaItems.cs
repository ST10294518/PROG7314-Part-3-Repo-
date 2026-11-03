using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Winx.API.Migrations
{
    /// <inheritdoc />
    public partial class AddMediaItems : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "MediaItem",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    TravelEntryId = table.Column<int>(type: "INTEGER", nullable: false),
                    MediaType = table.Column<string>(type: "TEXT", nullable: false),
                    FileName = table.Column<string>(type: "TEXT", nullable: false),
                    FilePath = table.Column<string>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MediaItem", x => x.Id);
                    table.ForeignKey(
                        name: "FK_MediaItem_TravelEntries_TravelEntryId",
                        column: x => x.TravelEntryId,
                        principalTable: "TravelEntries",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_MediaItem_TravelEntryId",
                table: "MediaItem",
                column: "TravelEntryId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "MediaItem");
        }
    }
}
