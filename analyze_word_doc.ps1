# 文档分析脚本
# 用法: .\analyze_word_doc.ps1

# 目标文档路径
$docPath = "6_毕业设计（论文）-计算机科学与技术专业2025年-熊恒.docx"

# 输出文件路径
$outputPath = "文档分析结果.txt"
$contentPath = "文档内容.txt"

# 检查文件是否存在
if (-not (Test-Path $docPath)) {
    Write-Output "文件不存在: $docPath"
    exit
}

Write-Output "开始分析文档: $(Split-Path $docPath -Leaf)"

try {
    # 创建Word应用实例
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    
    # 打开文档
    $doc = $word.Documents.Open($docPath)
    
    # 获取文档内容
    $text = $doc.Content.Text
    
    # 提取文档信息
    $paragraphs = $doc.Paragraphs.Count
    $words = $doc.Words.Count
    $pages = $doc.ComputeStatistics([Microsoft.Office.Interop.Word.WdStatistic]::wdStatisticPages)
    
    # 分析文档结构
    $headings = @()
    for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
        $para = $doc.Paragraphs.Item($i)
        if ($para.OutlineLevel -lt 9) { # 标题级别
            $headings += $para.Range.Text.Trim()
        }
    }
    
    # 关闭文档和Word应用
    $doc.Close()
    $word.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($doc) | Out-Null
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
    [System.GC]::Collect()
    [System.GC]::WaitForPendingFinalizers()
    
    # 保存文档分析结果
    $analysis = @"
=== 文档分析结果 ===

文件名: $(Split-Path $docPath -Leaf)
页数: $pages
段落数: $paragraphs
字数: $words

=== 文档结构 ===

$(($headings | ForEach-Object { $_ }) -join "`n")

=== 文档内容预览 ===

$(if ($text.Length -gt 1000) { $text.Substring(0, 1000) + "..." } else { $text })
"@
    
    # 保存分析结果
    $analysis | Out-File -FilePath $outputPath -Encoding utf8
    
    # 保存完整内容
    $text | Out-File -FilePath $contentPath -Encoding utf8
    
    Write-Output "分析结果已保存到: $outputPath"
    Write-Output "文档内容已保存到: $contentPath"
    
    # 显示分析结果概要
    Write-Output "`n文档概要:"
    Write-Output "- 页数: $pages"
    Write-Output "- 段落数: $paragraphs"
    Write-Output "- 字数: $words"
    Write-Output "- 标题数: $($headings.Count)"
    
} catch {
    Write-Output "分析文档时出错: $_"
} 