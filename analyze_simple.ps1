$docx = (Get-ChildItem -Filter "*.docx")[0].FullName
Write-Output "Found docx: $docx"

try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $doc = $word.Documents.Open($docx)
    $content = $doc.Content.Text
    $content | Out-File -FilePath "content.txt" -Encoding utf8
    Write-Output "Content saved to content.txt"
    $doc.Close()
    $word.Quit()
} catch {
    Write-Output "Error: $_"
} 