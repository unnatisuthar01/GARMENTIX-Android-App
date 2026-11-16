Add-Type -AssemblyName System.Drawing

$filePath = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable\kurti_01.png"
$drawableDir = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable"

$src = New-Object System.Drawing.Bitmap($filePath)
[int]$width = $src.Width
[int]$height = $src.Height
[int]$w1 = $width - 1
[int]$h1 = $height - 1

# Flood fill outer background
[bool[,]]$isBackground = New-Object 'bool[,]' $width, $height
$queue = New-Object System.Collections.Generic.Queue[System.Drawing.Point]

for ($x = 0; $x -lt $width; $x++) {
    $isBackground[$x, 0] = $true
    $isBackground[$x, $h1] = $true
    $queue.Enqueue((New-Object System.Drawing.Point($x, 0)))
    $queue.Enqueue((New-Object System.Drawing.Point($x, $h1)))
}

for ($y = 0; $y -lt $height; $y++) {
    $isBackground[0, $y] = $true
    $isBackground[$w1, $y] = $true
    $queue.Enqueue((New-Object System.Drawing.Point(0, $y)))
    $queue.Enqueue((New-Object System.Drawing.Point($w1, $y)))
}

while ($queue.Count -gt 0) {
    $curr = $queue.Dequeue()
    [int]$cx = $curr.X
    [int]$cy = $curr.Y

    # Do not leak into neck opening (X: 132..182, Y: 20..35)
    if ($cy -ge 20 -and $cy -le 35 -and $cx -ge 132 -and $cx -le 182) {
        continue
    }

    [int]$xR = $cx + 1
    [int]$xL = $cx - 1
    [int]$yD = $cy + 1
    [int]$yU = $cy - 1

    if ($xR -lt $width -and -not $isBackground[$xR, $cy]) {
        $p = $src.GetPixel($xR, $cy)
        if (($p.R + $p.G + $p.B) / 3.0 -gt 190) {
            $isBackground[$xR, $cy] = $true
            $queue.Enqueue((New-Object System.Drawing.Point($xR, $cy)))
        }
    }
    if ($xL -ge 0 -and -not $isBackground[$xL, $cy]) {
        $p = $src.GetPixel($xL, $cy)
        if (($p.R + $p.G + $p.B) / 3.0 -gt 190) {
            $isBackground[$xL, $cy] = $true
            $queue.Enqueue((New-Object System.Drawing.Point($xL, $cy)))
        }
    }
    if ($yD -lt $height -and -not $isBackground[$cx, $yD]) {
        $p = $src.GetPixel($cx, $yD)
        if (($p.R + $p.G + $p.B) / 3.0 -gt 190) {
            $isBackground[$cx, $yD] = $true
            $queue.Enqueue((New-Object System.Drawing.Point($cx, $yD)))
        }
    }
    if ($yU -ge 0 -and -not $isBackground[$cx, $yU]) {
        $p = $src.GetPixel($cx, $yU)
        if (($p.R + $p.G + $p.B) / 3.0 -gt 190) {
            $isBackground[$cx, $yU] = $true
            $queue.Enqueue((New-Object System.Drawing.Point($cx, $yU)))
        }
    }
}

function New-TransparentBitmap {
    return New-Object System.Drawing.Bitmap($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

# 1. High Contrast Shadows Overlay (kurti_01_shadows.png)
$bmpShadows = New-TransparentBitmap
for ($y = 0; $y -lt $height; $y++) {
    for ($x = 0; $x -lt $width; $x++) {
        $p = $src.GetPixel($x, $y)
        $brightness = ($p.R + $p.G + $p.B) / 3.0
        if ($brightness -lt 210) {
            $alpha = [int](255 * (1.0 - ($brightness / 220.0)))
            if ($alpha -gt 255) { $alpha = 255 }
            if ($alpha -lt 80) { $alpha = 80 }
            $bmpShadows.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, 10, 10, 10))
        }
    }
}

# 2. Garment Part Masks
$bmpBody = New-TransparentBitmap
$bmpSleeveLeft = New-TransparentBitmap
$bmpSleeveRight = New-TransparentBitmap
$bmpNeckline = New-TransparentBitmap
$bmpCuffs = New-TransparentBitmap

$whiteColor = [System.Drawing.Color]::FromArgb(255, 255, 255, 255)

for ($y = 0; $y -lt $height; $y++) {
    for ($x = 0; $x -lt $width; $x++) {
        if (-not $isBackground[$x, $y]) {
            $p = $src.GetPixel($x, $y)
            $brightness = ($p.R + $p.G + $p.B) / 3.0

            if ($brightness -ge 100) {
                # Neckline & Placket (X: 132..183, Y: 15..110)
                if ($y -ge 15 -and $y -le 110 -and $x -ge 132 -and $x -le 183) {
                    $bmpNeckline.SetPixel($x, $y, $whiteColor)
                }
                # Left Cuff (Y: 170..205)
                elseif ($y -ge 170 -and $y -le 205 -and $x -ge 63 -and $x -le 96) {
                    $bmpCuffs.SetPixel($x, $y, $whiteColor)
                }
                # Right Cuff (Y: 170..205)
                elseif ($y -ge 170 -and $y -le 205 -and $x -ge 218 -and $x -le 252) {
                    $bmpCuffs.SetPixel($x, $y, $whiteColor)
                }
                # Left Sleeve (Y: 20..170)
                elseif ($y -ge 20 -and $y -lt 170 -and $x -ge 63 -and $x -le 115) {
                    $bmpSleeveLeft.SetPixel($x, $y, $whiteColor)
                }
                # Right Sleeve (Y: 20..170)
                elseif ($y -ge 20 -and $y -lt 170 -and $x -ge 198 -and $x -le 252) {
                    $bmpSleeveRight.SetPixel($x, $y, $whiteColor)
                }
                # All remaining interior pixels are Body!
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

Write-Host "Seamless Kurti 01 masks generated without hairline gaps!"
