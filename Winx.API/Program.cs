using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi;
using Winx.API.Data;

var builder = WebApplication.CreateBuilder(args);

// Register API controllers
builder.Services.AddControllers();

// Register SQLite database
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(
        builder.Configuration.GetConnectionString("DefaultConnection")
        ?? "Data Source=winx.db"
    ));

// Register Swagger/OpenAPI
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

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.MapControllers();

app.Run();