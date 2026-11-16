Add-Type -AssemblyName System.Drawing

$filePath = "C:\Users\DELL\AndroidStudioProjects\Garmentix\app\src\main\res\drawable\kurti_01.png"
$bmp = New-Object System.Drawing.Bitmap($filePath)

$w = $bmp.Width
$h = $bmp.Height

$leftBound = New-Object 'int[]' $h
$rightBound = New-Object 'int[]' $h

for ($y = 0; $y -lt $h; $y++) {
    $leftBound[$y] = -1
    $rightBound[$y] = -1

    for ($x = 0; $x -lt $w; $x++) {
        $p = $bmp.GetPixel($x, $y)
        $b = ($p.R + $p.G + $p.B) / 3.0
        if ($b -lt 200) {
            if ($leftBound[$y] -eq -1) { $leftBound[$y] = $x }
            $rightBound[$y] = $x
        }
    }
}

for ($y = 0; $y -lt $h; $y += 10) {
    Write-Host "Y=$y : Left=$($leftBound[$y]) Right=$($rightBound[$y])"
}

$bmp.Dispose()
