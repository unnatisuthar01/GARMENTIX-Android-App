Add-Type -AssemblyName System.Drawing

$drawableDir = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable"
$files = @("kurti_01_body.png", "kurti_01_sleeve_left.png", "kurti_01_sleeve_right.png", "kurti_01_neckline.png", "kurti_01_cuffs.png", "kurti_01_shadows.png")

foreach ($file in $files) {
    $path = Join-Path $drawableDir $file
    if (Test-Path $path) {
        $bmp = New-Object System.Drawing.Bitmap($path)
        [int]$opaqueCount = 0
        [int]$transCount = 0

        for ($y = 0; $y -lt $bmp.Height; $y++) {
            for ($x = 0; $x -lt $bmp.Width; $x++) {
                $p = $bmp.GetPixel($x, $y)
                if ($p.A -gt 10) {
                    $opaqueCount++
                } else {
                    $transCount++
                }
            }
        }
        Write-Host "$file -> OpaquePixels=$opaqueCount, TransPixels=$transCount"
        $bmp.Dispose()
    } else {
        Write-Host "MISSING: $file"
    }
}
