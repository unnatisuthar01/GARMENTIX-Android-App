Add-Type -AssemblyName System.Drawing

$drawableDir = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable"
$templates = @(
    "kurti_01.png",
    "kurti_02.jpg",
    "top_01.png",
    "top_02.png",
    "gown_01.png",
    "gown_02.jpg",
    "ic_cloth_sample.png",
    "dress_02.png",
    "jeans_01.jpg",
    "jeans_02.jpg",
    "tshirt_01.jpg",
    "tshirt_02.jpg",
    "jacket_01.jpg",
    "jacket_02.jpg",
    "shirt_01.jpg",
    "shirt_02.jpg"
)

foreach ($file in $templates) {
    $path = Join-Path $drawableDir $file
    if (Test-Path $path) {
        $bmp = New-Object System.Drawing.Bitmap($path)
        Write-Host "$file -> Width=$($bmp.Width), Height=$($bmp.Height), Format=$($bmp.PixelFormat)"
        $bmp.Dispose()
    } else {
        Write-Host "MISSING: $file"
    }
}
