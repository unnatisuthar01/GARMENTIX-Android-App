Add-Type -AssemblyName System.Drawing

$filePath = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable\kurti_01.png"
$drawableDir = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable"

$src = New-Object System.Drawing.Bitmap($filePath)
$w = $src.Width
$h = $src.Height

$leftBound = New-Object 'int[]' $h
$rightBound = New-Object 'int[]' $h

for ($y = 0; $y -lt $h; $y++) {
    $leftBound[$y] = -1
    $rightBound[$y] = -1

    for ($x = 0; $x -lt $w; $x++) {
        $p = $src.GetPixel($x, $y)
        $b = ($p.R + $p.G + $p.B) / 3.0
        if ($b -lt 200) {
            if ($leftBound[$y] -eq -1) { $leftBound[$y] = $x }
            $rightBound[$y] = $x
        }
    }
}

function New-TransparentBitmap {
    return New-Object System.Drawing.Bitmap($w, $h, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

$bmpBody = New-TransparentBitmap
$bmpSleeveLeft = New-TransparentBitmap
$bmpSleeveRight = New-TransparentBitmap
$bmpNeckline = New-TransparentBitmap
$bmpCuffs = New-TransparentBitmap
$bmpShadows = New-TransparentBitmap

$whiteColor = [System.Drawing.Color]::FromArgb(255, 255, 255, 255)

for ($y = 0; $y -lt $h; $y++) {
    if ($leftBound[$y] -ne -1 -and $rightBound[$y] -ne -1) {
        $minX = $leftBound[$y]
        $maxX = $rightBound[$y]

        for ($x = $minX; $x -le $maxX; $x++) {
            $p = $src.GetPixel($x, $y)
            $b = ($p.R + $p.G + $p.B) / 3.0

            if ($b -lt 210) {
                $alpha = [int](255 * (1.0 - ($b / 220.0)))
                if ($alpha -gt 255) { $alpha = 255 }
                if ($alpha -lt 50) { $alpha = 50 }
                $bmpShadows.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, 15, 15, 15))
            }

            if ($b -ge 120) {
                if ($y -ge 15 -and $y -le 110 -and $x -ge 132 -and $x -le 183) {
                    $bmpNeckline.SetPixel($x, $y, $whiteColor)
                }
                elseif ($y -ge 170 -and $y -le 205 -and $x -ge $minX -and $x -le 96) {
                    $bmpCuffs.SetPixel($x, $y, $whiteColor)
                }
                elseif ($y -ge 170 -and $y -le 205 -and $x -ge 219 -and $x -le $maxX) {
                    $bmpCuffs.SetPixel($x, $y, $whiteColor)
                }
                elseif ($y -ge 20 -and $y -lt 170 -and $x -ge $minX -and $x -le 114) {
                    $bmpSleeveLeft.SetPixel($x, $y, $whiteColor)
                }
                elseif ($y -ge 20 -and $y -lt 170 -and $x -ge 201 -and $x -le $maxX) {
                    $bmpSleeveRight.SetPixel($x, $y, $whiteColor)
                }
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

Write-Host "Pixel-perfect silhouette masks generated successfully!"
