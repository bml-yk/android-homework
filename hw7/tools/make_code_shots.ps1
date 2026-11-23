Add-Type -AssemblyName System.Drawing

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$OutputDir = Join-Path $ProjectRoot "docs\screenshots"
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

function New-CodeScreenshot {
    param(
        [string]$SourcePath,
        [string]$OutputPath,
        [string]$Title,
        [string]$Subtitle,
        [string]$Ranges
    )

    $sourceLines = Get-Content -LiteralPath $SourcePath
    $selected = [System.Collections.Generic.List[object]]::new()
    foreach ($range in $Ranges.Split(",")) {
        $parts = $range.Split("-")
        $first = [int]$parts[0]
        $last = [int]$parts[1]
        for ($lineNumber = $first; $lineNumber -le $last; $lineNumber++) {
            $selected.Add([pscustomobject]@{
                Number = $lineNumber
                Text = $sourceLines[$lineNumber - 1]
            })
        }
        if ($range -ne $Ranges.Split(",")[-1]) {
            $selected.Add([pscustomobject]@{ Number = 0; Text = "    // ..." })
        }
    }

    $width = 1800
    $lineHeight = 34
    $headerHeight = 125
    $bottomPadding = 38
    $height = $headerHeight + ($selected.Count * $lineHeight) + $bottomPadding
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint =
        [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

    $background = [System.Drawing.ColorTranslator]::FromHtml("#0D151C")
    $header = [System.Drawing.ColorTranslator]::FromHtml("#17232C")
    $orange = [System.Drawing.ColorTranslator]::FromHtml("#F1A864")
    $muted = [System.Drawing.ColorTranslator]::FromHtml("#84939B")
    $text = [System.Drawing.ColorTranslator]::FromHtml("#E9EDF0")
    $green = [System.Drawing.ColorTranslator]::FromHtml("#8CC8A6")

    $graphics.Clear($background)
    $headerBrush = [System.Drawing.SolidBrush]::new($header)
    $graphics.FillRectangle($headerBrush, 0, 0, $width, 104)

    $titleFont = [System.Drawing.Font]::new(
        "Microsoft YaHei UI",
        24,
        [System.Drawing.FontStyle]::Bold
    )
    $subtitleFont = [System.Drawing.Font]::new(
        "Microsoft YaHei UI",
        14,
        [System.Drawing.FontStyle]::Regular
    )
    $codeFont = [System.Drawing.Font]::new(
        "Cascadia Mono",
        17,
        [System.Drawing.FontStyle]::Regular
    )
    $lineNumberFont = [System.Drawing.Font]::new(
        "Cascadia Mono",
        14,
        [System.Drawing.FontStyle]::Regular
    )

    $titleBrush = [System.Drawing.SolidBrush]::new($text)
    $orangeBrush = [System.Drawing.SolidBrush]::new($orange)
    $mutedBrush = [System.Drawing.SolidBrush]::new($muted)
    $textBrush = [System.Drawing.SolidBrush]::new($text)
    $greenBrush = [System.Drawing.SolidBrush]::new($green)

    $graphics.DrawString($Title, $titleFont, $titleBrush, 42, 20)
    $graphics.DrawString($Subtitle, $subtitleFont, $orangeBrush, 44, 64)

    $y = $headerHeight
    foreach ($line in $selected) {
        if ($line.Number -eq 0) {
            $graphics.DrawString($line.Text, $codeFont, $mutedBrush, 100, $y)
        } else {
            $lineNumber = $line.Number.ToString().PadLeft(3, " ")
            $graphics.DrawString($lineNumber, $lineNumberFont, $mutedBrush, 34, $y + 3)
            $codeText = $line.Text.Replace("`t", "    ")
            $codeBrush = if ($codeText.TrimStart().StartsWith("//")) {
                $greenBrush
            } else {
                $textBrush
            }
            $graphics.DrawString($codeText, $codeFont, $codeBrush, 100, $y)
        }
        $y += $lineHeight
    }

    $bitmap.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
    $titleFont.Dispose()
    $subtitleFont.Dispose()
    $codeFont.Dispose()
    $lineNumberFont.Dispose()
    $headerBrush.Dispose()
    $titleBrush.Dispose()
    $orangeBrush.Dispose()
    $mutedBrush.Dispose()
    $textBrush.Dispose()
    $greenBrush.Dispose()
}

New-CodeScreenshot `
    -SourcePath (Join-Path $ProjectRoot "app\src\main\java\cn\edu\sicnucs\bml\servicemusicplayer\data\AudioRepository.kt") `
    -OutputPath (Join-Path $OutputDir "code_content_resolver.png") `
    -Title "AudioRepository.kt" `
    -Subtitle "ContentResolver 查询 MediaStore 音频列表" `
    -Ranges "18-30,39-46"

New-CodeScreenshot `
    -SourcePath (Join-Path $ProjectRoot "app\src\main\java\cn\edu\sicnucs\bml\servicemusicplayer\player\MusicService.kt") `
    -OutputPath (Join-Path $OutputDir "code_service_binder.png") `
    -Title "MusicService.kt" `
    -Subtitle "Binder 暴露播放命令，独立线程驱动 MediaPlayer" `
    -Ranges "25-42,64-73,146-158"

New-CodeScreenshot `
    -SourcePath (Join-Path $ProjectRoot "app\src\main\java\cn\edu\sicnucs\bml\servicemusicplayer\MainActivity.kt") `
    -OutputPath (Join-Path $OutputDir "code_seekbar_binding.png") `
    -Title "MainActivity.kt" `
    -Subtitle "绑定服务并通过 SeekBar 显示和调整播放进度" `
    -Ranges "106-145,247-272"

New-CodeScreenshot `
    -SourcePath (Join-Path $ProjectRoot "app\src\main\java\cn\edu\sicnucs\bml\servicemusicplayer\player\MusicService.kt") `
    -OutputPath (Join-Path $OutputDir "code_notification.png") `
    -Title "MusicService.kt" `
    -Subtitle "以前台服务发布当前歌曲通知" `
    -Ranges "275-302,304-318"
