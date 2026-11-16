Add-Type -AssemblyName System.Drawing

$filePath = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable\kurti_01.png"
$drawableDir = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable"

$src = New-Object System.Drawing.Bitmap($filePath)
[int]$width = $src.Width
[int]$height = $src.Height

# Scan row-by-row for exact outer garment silhouette
$leftBound = New-Object 'int[]' $height
$rightBound = New-Object 'int[]' $height

for ($y = 0; $y -lt $height; $y++) {
    $leftBound[$y] = -1
    $rightBound[$y] = -1

    for ($x = 0; $x -lt $width; $x++) {
        $p = $src.GetPixel($x, $y)
        $b = ($p.R + $p.G + $p.B) / 3.0
        if ($b -lt 200) { # Line art / outline
            if ($leftBound[$y] -eq -1) { $leftBound[$y] = $x }
            $rightBound[$y] = $x
        }
    }
}

function New-TransparentBitmap {
    return New-Object System.Drawing.Bitmap($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

$bmpBody = New-TransparentBitmap
$bmpSleeveLeft = New-TransparentBitmap
$bmpSleeveRight = New-TransparentBitmap
$bmpNeckline = New-TransparentBitmap
$bmpCuffs = New-TransparentBitmap
$bmpShadows = New-TransparentBitmap

$whiteColor = [System.Drawing.Color]::FromArgb(255, 255, 255, 255)

# 1. High-Contrast Shadows & Outlines Overlay (kurti_01_shadows.png)
for ($y = 0; $y -lt $height; $y++) {
    for ($x = 0; $x -lt $width; $x++) {
        $p = $src.GetPixel($x, $y)
        $brightness = ($p.R + $p.G + $p.B) / 3.0
        if ($brightness -lt 210) {
            $alpha = [int](255 * (1.0 - ($brightness / 220.0)))
            if ($alpha -gt 255) { $alpha = 255 }
            if ($alpha -lt 70) { $alpha = 70 }
            $bmpShadows.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, 10, 10, 10))
        }
    }
}

# 2. Seamless Fill Masks using Silhouette Row Bounds
for ($y = 20; $y -le 375; $y++) {
    if ($leftBound[$y] -ne -1 -and $rightBound[$y] -ne -1) {
        $minX = $leftBound[$y]
        $maxX = $rightBound[$y]

        for ($x = $minX; $x -le $maxX; $x++) {
            $p = $src.GetPixel($x, $y)
            $brightness = ($p.R + $p.G + $p.B) / 3.0

            if ($brightness -ge 120) {
                # Neckline & Placket (top center)
                if ($y -ge 18 -and $y -le 115 -and $x -ge 132 -and $x -le 183) {
                    $bmpNeckline.SetPixel($x, $y, $whiteColor)
                }
                # Left Cuff Band (Y: 170..205 at sleeve end)
                elseif ($y -ge 170 -and $y -le 205 -and $x -ge $minX -and $x -le 96) {
                    $bmpCuffs.SetPixel($x, $y, $whiteColor)
                }
                # Right Cuff Band (Y: 170..205 at sleeve end)
                elseif ($y -ge 170 -and $y -le 205 -and $x -ge 218 -and $x -le $maxX) {
                    $bmpCuffs.SetPixel($x, $y, $whiteColor)
                }
                # Left Sleeve (Y: 20..170)
                elseif ($y -ge 20 -and $y -lt 170 -and $x -ge $minX -and $x -le 114) {
                    $bmpSleeveLeft.SetPixel($x, $y, $whiteColor)
                }
                # Right Sleeve (Y: 20..170)
                elseif ($y -ge 20 -and $y -lt 170 -and $x -ge 198 -and $x -le $maxX) {
                    $bmpSleeveRight.SetPixel($x, $y, $whiteColor)
                }
                # ALL REMAINING INTERIOR SILHOUETTE PIXELS ARE BODY!
                else {
                    $bmpBody.SetPixel($x, $y, $whiteColor)
                }
            }
        }
    }
}

$bmpBody.Save("$drawableDir\kurti_01_body.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmpSleeveLeft.Save("$drawableDir\kurti_01_sleeve_left.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmpSleeveRight.Save("$drawableDir\kurti_01_sleeve_right.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmpNeckline.Save("$drawableDir\kurti_01_neckline.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmpCuffs.Save("$drawableDir\kurti_01_cuffs.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmpShadows.Save("$drawableDir\kurti_01_shadows.png", [System.Drawing.Imaging.ImageFormat]::Png)

$bmpBody.Dispose()
$bmpSleeveLeft.Dispose()
$bmpSleeveRight.Dispose()
$bmpNeckline.Dispose()
$bmpCuffs.Dispose()
$bmpShadows.Dispose()
$src.Dispose()

Write-Host "Seamless silhouette masks generated successfully!"
