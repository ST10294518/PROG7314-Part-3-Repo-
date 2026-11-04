using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi;
using Winx.API.Data;

var builder = WebApplication.CreateBuilder(args);

// Register API controllers.
builder.Services.AddControllers();

// Register the SQLite database.
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(
        builder.Configuration.GetConnectionString("DefaultConnection")
        ?? "Data Source=winx.db"
    ));

// Register Swagger/OpenAPI.
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Winx REST API",
        Version = "v1",
        Description = "REST API for the Winx travel and restaurant journal."
    });
});

var app = builder.Build();

// Upgrade an EXISTING SQLite MediaItem table without dropping any data.
// This targeted upgrade is for databases created before MediaItem.UploadId existed.
// It is safe to run on subsequent starts: both operations are conditional.
// It deliberately does not call EnsureCreated() or Migrate(), because those
// could conflict with the project's existing database initialization history.
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    var connection = db.Database.GetDbConnection();
    await connection.OpenAsync();
    try
    {
        if (connection is not SqliteConnection sqlite)
            throw new InvalidOperationException("Winx database upgrade requires SQLite.");

        await using var transaction = (SqliteTransaction)await sqlite.BeginTransactionAsync();

        // Do not fabricate a new database schema if the existing MediaItem table
        // is missing; the normal project database setup must handle fresh installs.
        await using var tableCheck = sqlite.CreateCommand();
        tableCheck.Transaction = transaction;
        tableCheck.CommandText = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'MediaItem';";
        var tableExists = Convert.ToInt64(await tableCheck.ExecuteScalarAsync()) > 0;

        if (tableExists)
        {
            var uploadIdExists = false;
            await using (var columnCheck = sqlite.CreateCommand())
            {
                columnCheck.Transaction = transaction;
                columnCheck.CommandText = "PRAGMA table_info(\"MediaItem\");";
                await using var reader = await columnCheck.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                {
                    if (string.Equals(reader.GetString(1), "UploadId", StringComparison.OrdinalIgnoreCase))
                    {
                        uploadIdExists = true;
                        break;
                    }
                }
            }

            if (!uploadIdExists)
            {
                await using var addColumn = sqlite.CreateCommand();
                addColumn.Transaction = transaction;
                addColumn.CommandText = "ALTER TABLE \"MediaItem\" ADD COLUMN \"UploadId\" TEXT NULL;";
                await addColumn.ExecuteNonQueryAsync();
            }

            // SQLite allows multiple NULL values in a UNIQUE index, so old
            // media records without UploadId remain valid.
            await using var createIndex = sqlite.CreateCommand();
            createIndex.Transaction = transaction;
            createIndex.CommandText =
                "CREATE UNIQUE INDEX IF NOT EXISTS \"IX_MediaItem_UploadId\" ON \"MediaItem\" (\"UploadId\");";
            await createIndex.ExecuteNonQueryAsync();
        }

        await transaction.CommitAsync();
    }
    finally
    {
        await connection.CloseAsync();
    }
}

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Make uploaded media accessible at /uploads/<generated-filename>.
Directory.CreateDirectory(Path.Combine(app.Environment.ContentRootPath, "wwwroot", "uploads"));
app.UseStaticFiles();

app.MapControllers();

app.Run();
