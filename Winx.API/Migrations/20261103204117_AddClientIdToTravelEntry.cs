using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Winx.API.Migrations
{
    /// <inheritdoc />
    public partial class AddClientIdToTravelEntry : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<Guid>(
                name: "ClientId",
                table: "TravelEntries",
                type: "TEXT",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_TravelEntries_ClientId",
                table: "TravelEntries",
                column: "ClientId",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_TravelEntries_ClientId",
                table: "TravelEntries");

            migrationBuilder.DropColumn(
                name: "ClientId",
                table: "TravelEntries");
        }
    }
}
