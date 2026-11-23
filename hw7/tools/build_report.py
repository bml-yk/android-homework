from __future__ import annotations

from pathlib import Path

from docx import Document
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt
from PIL import Image, ImageDraw, ImageFont


PROJECT_ROOT = Path(__file__).resolve().parents[1]
SOURCE_DOCX = Path(r"D:\SoftWare\实验七+基于Service的音乐播放应用.docx")
OUTPUT_DOCX = (
    PROJECT_ROOT
    / "docs"
    / "实验七_基于Service的音乐播放应用_完成版.docx"
)
SCREENSHOT_DIR = PROJECT_ROOT / "docs" / "screenshots"


def make_runtime_composite() -> Path:
    left = Image.open(SCREENSHOT_DIR / "runtime_list.png").convert("RGB")
    right = Image.open(SCREENSHOT_DIR / "runtime_playing.png").convert("RGB")
    target_w = 1040
    target_h = round(left.height * target_w / left.width)
    left = left.resize((target_w, target_h), Image.Resampling.LANCZOS)
    right = right.resize((target_w, target_h), Image.Resampling.LANCZOS)

    canvas = Image.new("RGB", (2200, target_h + 110), "#0d151c")
    canvas.paste(left, (40, 100))
    canvas.paste(right, (1120, 100))
    draw = ImageDraw.Draw(canvas)
    font_path = Path(r"C:\Windows\Fonts\msyh.ttc")
    font = (
        ImageFont.truetype(str(font_path), 42)
        if font_path.exists()
        else ImageFont.load_default()
    )
    draw.text((48, 28), "本地音乐列表", fill="#f7f3ec", font=font)
    draw.text((1128, 28), "播放进度与实时状态", fill="#f7f3ec", font=font)

    output = SCREENSHOT_DIR / "runtime_main_and_playing.png"
    canvas.save(output, quality=95)
    return output


def make_notification_detail() -> Path:
    source = Image.open(
        SCREENSHOT_DIR / "runtime_notification.png"
    ).convert("RGB")
    crop = source.crop((35, 700, 1045, 1138))
    canvas = Image.new("RGB", (1010, 500), "#0d151c")
    canvas.paste(crop, (0, 62))
    draw = ImageDraw.Draw(canvas)
    font_path = Path(r"C:\Windows\Fonts\msyh.ttc")
    font = (
        ImageFont.truetype(str(font_path), 34)
        if font_path.exists()
        else ImageFont.load_default()
    )
    draw.text(
        (18, 10),
        "通知栏显示歌曲名和播放控制",
        fill="#f1a864",
        font=font,
    )
    output = SCREENSHOT_DIR / "notification_detail.png"
    canvas.save(output)
    return output


def clear_runs(paragraph) -> None:
    for child in list(paragraph._p):
        if child.tag == qn("w:r"):
            paragraph._p.remove(child)


def set_paragraph_text(paragraph, text: str, *, bold: bool = False) -> None:
    clear_runs(paragraph)
    run = paragraph.add_run(text)
    run.bold = bold
    set_font(run, "宋体", 10.5)


def set_font(run, name: str, size: float, *, bold=None) -> None:
    run.font.name = name
    run.font.size = Pt(size)
    if bold is not None:
        run.bold = bold
    run._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), name)
    run._element.get_or_add_rPr().rFonts.set(qn("w:ascii"), name)
    run._element.get_or_add_rPr().rFonts.set(qn("w:hAnsi"), name)


def add_text(
    anchor,
    text: str,
    *,
    bold: bool = False,
    size: float = 10.5,
    align=None,
    first_line: bool = True,
    keep_with_next: bool = False,
):
    paragraph = anchor.insert_paragraph_before()
    paragraph.paragraph_format.space_before = Pt(0)
    paragraph.paragraph_format.space_after = Pt(4)
    paragraph.paragraph_format.line_spacing = 1.3
    paragraph.paragraph_format.first_line_indent = (
        Pt(size * 2) if first_line and not bold else Pt(0)
    )
    paragraph.paragraph_format.keep_with_next = keep_with_next
    if align is not None:
        paragraph.alignment = align
    run = paragraph.add_run(text)
    set_font(run, "宋体", size, bold=bold)
    return paragraph


def add_label(anchor, text: str):
    return add_text(
        anchor,
        text,
        bold=True,
        size=12,
        first_line=False,
        keep_with_next=True,
    )


def add_picture(
    anchor,
    image_path: Path,
    alt_text: str,
    *,
    width_inches: float = 5.35,
    caption: str | None = None,
) -> None:
    paragraph = anchor.insert_paragraph_before()
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    paragraph.paragraph_format.space_before = Pt(4)
    paragraph.paragraph_format.space_after = Pt(2)
    paragraph.paragraph_format.keep_with_next = True
    run = paragraph.add_run()
    shape = run.add_picture(str(image_path), width=Inches(width_inches))
    shape._inline.docPr.set("descr", alt_text)
    shape._inline.docPr.set("title", alt_text)
    if caption:
        caption_paragraph = anchor.insert_paragraph_before()
        caption_paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        caption_paragraph.paragraph_format.space_before = Pt(0)
        caption_paragraph.paragraph_format.space_after = Pt(7)
        caption_run = caption_paragraph.add_run(caption)
        set_font(caption_run, "宋体", 9)
        caption_run.italic = True


def set_cell_shading(cell, fill: str) -> None:
    tc_pr = cell._tc.get_or_add_tcPr()
    shading = tc_pr.find(qn("w:shd"))
    if shading is None:
        shading = OxmlElement("w:shd")
        tc_pr.append(shading)
    shading.set(qn("w:fill"), fill)


def set_table_borders(table) -> None:
    tbl_pr = table._tbl.tblPr
    borders = tbl_pr.first_child_found_in("w:tblBorders")
    if borders is None:
        borders = OxmlElement("w:tblBorders")
        tbl_pr.append(borders)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = "w:" + edge
        element = borders.find(qn(tag))
        if element is None:
            element = OxmlElement(tag)
            borders.append(element)
        element.set(qn("w:val"), "single")
        element.set(qn("w:sz"), "4")
        element.set(qn("w:space"), "0")
        element.set(qn("w:color"), "D9D9D9")


def prevent_row_split(row) -> None:
    tr_pr = row._tr.get_or_add_trPr()
    cant_split = OxmlElement("w:cantSplit")
    tr_pr.append(cant_split)


def repeat_header(row) -> None:
    tr_pr = row._tr.get_or_add_trPr()
    header = OxmlElement("w:tblHeader")
    tr_pr.append(header)


def add_table(
    anchor,
    headers: list[str],
    rows: list[list[str]],
    widths: list[float],
):
    table = anchor._parent.add_table(
        rows=1,
        cols=len(headers),
        width=Inches(sum(widths)),
    )
    anchor._p.addprevious(table._tbl)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table)

    for column, (header, width) in enumerate(zip(headers, widths)):
        cell = table.rows[0].cells[column]
        cell.width = Inches(width)
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        set_cell_shading(cell, "1F4E78")
        paragraph = cell.paragraphs[0]
        paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        paragraph.paragraph_format.space_after = Pt(0)
        run = paragraph.add_run(header)
        set_font(run, "宋体", 9, bold=True)
        run.font.color.rgb = __import__("docx").shared.RGBColor(255, 255, 255)
    repeat_header(table.rows[0])
    prevent_row_split(table.rows[0])

    for row_index, values in enumerate(rows):
        row_cells = table.add_row().cells
        prevent_row_split(table.rows[-1])
        for column, (value, width) in enumerate(zip(values, widths)):
            cell = row_cells[column]
            cell.width = Inches(width)
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            set_cell_shading(cell, "F5F8FA" if row_index % 2 == 1 else "FFFFFF")
            paragraph = cell.paragraphs[0]
            paragraph.paragraph_format.space_before = Pt(1)
            paragraph.paragraph_format.space_after = Pt(1)
            paragraph.paragraph_format.line_spacing = 1.1
            if column == 0 or len(value) <= 10:
                paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
            run = paragraph.add_run(value)
            set_font(run, "宋体", 8.7)

    spacer = anchor.insert_paragraph_before()
    spacer.paragraph_format.space_after = Pt(2)
    return table


def main() -> None:
    SCREENSHOT_DIR.mkdir(parents=True, exist_ok=True)
    runtime_composite = make_runtime_composite()
    notification_detail = make_notification_detail()

    document = Document(SOURCE_DOCX)
    document.core_properties.author = "毕美灵"
    document.core_properties.title = "实验七 基于Service的音乐播放应用"

    # Fill the template header while preserving the report title formatting.
    header_line = document.paragraphs[0]
    clear_runs(header_line)
    run = header_line.add_run("实验编号：  7    ")
    set_font(run, "宋体", 10.5)
    run = header_line.add_run("Android移动应用开发 实验报告")
    set_font(run, "宋体", 16, bold=True)
    run = header_line.add_run("    2026.11.23")
    set_font(run, "宋体", 10.5)

    set_paragraph_text(
        document.paragraphs[2],
        "姓名：毕美灵   学号：2024110101   指导老师：李贵洋   实验成绩：____",
    )
    for run in document.paragraphs[2].runs:
        run.font.size = Pt(9.4)
    set_paragraph_text(
        document.paragraphs[5],
        "项目地址：https://github.com/bml-yk/android-homework/tree/Frag/hw7",
        bold=True,
    )

    core_heading = document.paragraphs[36]
    analysis_heading = document.paragraphs[54]
    final_note = document.paragraphs[71]
    core_heading.paragraph_format.page_break_before = True
    core_heading.paragraph_format.keep_with_next = True
    analysis_heading.paragraph_format.keep_with_next = True

    # Remove the blank writing space from the supplied template.
    for paragraph in list(document.paragraphs[37:54]) + list(
        document.paragraphs[55:71]
    ):
        parent = paragraph._element.getparent()
        if parent is not None:
            parent.remove(paragraph._element)

    add_text(
        analysis_heading,
        "本项目采用 Activity、绑定 Service 和 MediaPlayer 组成播放器。"
        "MainActivity 负责权限申请、音乐列表查询和界面显示，MusicService "
        "保存播放队列、控制 MediaPlayer 并在独立线程中更新播放状态。"
        "Activity 通过 Binder 调用服务命令，并接收服务回传的不可变状态快照，"
        "从而将界面显示与播放逻辑分离。",
    )

    add_label(analysis_heading, "3.1 开发环境与项目结构")
    add_table(
        analysis_heading,
        ["项目", "配置"],
        [
            ["开发语言", "Kotlin，XML Views，Material 3"],
            ["构建环境", "Android Gradle Plugin 9.4.1，compileSdk / targetSdk 37"],
            ["最低版本", "Android 7.0（API 24）"],
            ["验证设备", "Pixel 8 模拟器，Android API 37，1080 × 2400"],
            ["核心类", "MainActivity、AudioRepository、MusicService、TrackAdapter"],
        ],
        [1.45, 4.2],
    )

    add_label(analysis_heading, "3.2 使用 ContentResolver 获取音乐列表")
    add_picture(
        analysis_heading,
        SCREENSHOT_DIR / "code_content_resolver.png",
        "AudioRepository 使用 ContentResolver 查询本地音乐的主要代码",
        width_inches=5.2,
        caption="图 1  ContentResolver 查询 MediaStore 音频数据",
    )
    add_text(
        analysis_heading,
        "AudioRepository 查询 MediaStore.Audio.Media.EXTERNAL_CONTENT_URI，"
        "只投影歌曲编号、标题、文件名、艺术家和时长。查询条件限定为音乐文件，"
        "并过滤不足 1 秒的无效记录，结果按标题排序。Cursor 通过 use 自动关闭，"
        "每条记录再使用 ContentUris.withAppendedId 生成可交给 MediaPlayer 的 URI。"
        "当艺术家字段为空或为 unknown 时，界面统一显示“未知艺术家”。",
    )

    add_label(analysis_heading, "3.3 Service、Binder 与多线程播放")
    add_picture(
        analysis_heading,
        SCREENSHOT_DIR / "code_service_binder.png",
        "MusicService 中 Binder 命令和独立播放线程的主要代码",
        width_inches=5.35,
        caption="图 2  MusicService 通过 Binder 暴露播放功能",
    )
    add_text(
        analysis_heading,
        "MusicBinder 暴露播放队列、播放、暂停、停止、上一首、下一首、"
        "跳转进度、状态快照和监听器注册等方法。所有对 MediaPlayer 的访问都"
        "投递到名为 MusicPlaybackThread 的 HandlerThread 中，避免界面线程被"
        "音频准备和资源释放阻塞。每 250 ms 在播放线程读取 currentPosition，"
        "再通过主线程 Handler 把状态快照交给 Activity。MediaPlayer 的"
        "Prepared、Completion 和 Error 回调也统一在播放线程处理。",
    )
    add_text(
        analysis_heading,
        "服务在切换歌曲前先注销旧监听器并释放旧的 MediaPlayer，防止异步回调"
        "把前一首歌曲的信息写回界面。播放完成后自动切换到队列下一首，"
        "队列末尾则回到第一首；停止时释放播放器、保留当前选择并撤销通知。",
    )

    add_label(analysis_heading, "3.4 SeekBar 实时进度与拖动跳转")
    add_picture(
        analysis_heading,
        SCREENSHOT_DIR / "code_seekbar_binding.png",
        "MainActivity 中 SeekBar 监听、播放进度显示和跳转代码",
        width_inches=5.2,
        caption="图 3  Activity 通过 Binder 更新和调整播放进度",
    )
    add_text(
        analysis_heading,
        "服务回传状态后，Activity 将 position / duration 换算为 0 到 1000"
        "的进度值。用户开始拖动时设置 isSeeking 标志，阻止周期性回调覆盖"
        "手指位置；停止拖动后再把百分比换算成毫秒并调用 Binder.seekTo。"
        "实测中播放进度先显示 0:04，拖动后变为 0:13，说明显示与跳转均正常。",
    )

    add_label(analysis_heading, "3.5 Notification 显示当前歌曲")
    add_picture(
        analysis_heading,
        notification_detail,
        "通知栏显示夜色回声歌曲名、播放状态和控制按钮",
        width_inches=4.3,
        caption="图 4  前台音乐通知显示歌曲名及播放控制",
    )
    add_text(
        analysis_heading,
        "开始播放后，MusicService 以 mediaPlayback 类型进入前台，并创建"
        "低优先级通知渠道。通知标题取当前歌曲名，正文显示播放状态和艺术家，"
        "同时提供上一首、暂停或播放、下一首操作。测试日志确认通知包含"
        "FOREGROUND_SERVICE 标志，服务 types=0x2，前台通知 ID 为 701；"
        "停止播放后通知被及时移除。",
    )
    add_picture(
        analysis_heading,
        SCREENSHOT_DIR / "code_notification.png",
        "MusicService 构建前台音乐通知的主要代码",
        width_inches=4.85,
        caption="图 5  前台音乐通知的构建与更新",
    )

    add_label(analysis_heading, "3.6 作品运行界面")
    add_picture(
        analysis_heading,
        runtime_composite,
        "应用主界面和正在播放界面的并排截图",
        width_inches=4.85,
        caption="图 6  本地音乐列表与实时播放界面",
    )
    add_text(
        analysis_heading,
        "主界面显示应用标题“毕美灵2024110101”、音乐库数量、当前歌曲、"
        "时间进度和全部歌曲列表。播放时状态文字由“已暂停”切换为"
        "“正在播放”，橙色进度条随 250 ms 状态快照向前移动，"
        "播放按钮同步变为“暂停”。",
    )

    add_label(analysis_heading, "3.7 调试问题与处理")
    add_text(
        analysis_heading,
        "调试过程中主要处理了四类问题：Android 13 以上改用 "
        "READ_MEDIA_AUDIO；切歌前注销旧监听器并及时释放旧 MediaPlayer，"
        "避免异步回调覆盖新歌曲状态；用 isSeeking 阻止 SeekBar 周期刷新与"
        "手指拖动互相干扰；在清单中声明并传入 "
        "FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK，使媒体通知在前台服务中"
        "稳定运行。",
    )

    page_break = analysis_heading.insert_paragraph_before()
    page_break.add_run().add_break(WD_BREAK.PAGE)
    add_table(
        analysis_heading,
        ["测试项", "操作与输入", "预期结果", "实测结果"],
        [
            ["音频查询", "授权后刷新 MediaStore", "显示本地音乐列表", "通过，共 3 首测试音频"],
            ["播放", "点击歌曲或播放按钮", "时间与进度增加", "通过，状态为“正在播放”"],
            ["暂停", "点击暂停按钮", "播放停止但保留位置", "通过，状态为“已暂停”"],
            ["进度跳转", "拖动 SeekBar 到约 65%", "歌曲从新位置继续", "通过，0:04 跳至 0:13"],
            ["上一首/下一首", "点击切歌按钮", "标题和队列序号同步变化", "通过，切换至第 2 / 3 首"],
            ["停止", "点击停止按钮", "释放播放器并移除通知", "通过，状态为“已停止”"],
            ["通知栏", "开始播放并展开通知栏", "显示歌曲名和播放控制", "通过，显示“夜色回声”"],
        ],
        [1.15, 1.5, 1.5, 1.55],
    )
    add_text(
        analysis_heading,
        "表 1  核心功能测试记录",
        size=9,
        align=WD_ALIGN_PARAGRAPH.CENTER,
        first_line=False,
    )

    add_text(
        final_note,
        "应用已经覆盖实验要求的本地音乐查询、服务内播放控制、Activity 绑定"
        "Binder、实时 SeekBar 和通知栏歌曲名。播放线程与界面线程相互分离，"
        "播放器状态通过单一快照传递，减少了界面与服务之间的耦合。"
        "在 API 37 模拟器中使用三首本地 WAV 音频测试，列表、播放、暂停、"
        "停止、切歌、拖动进度和前台通知均符合预期。",
    )
    add_label(final_note, "4.1 结果分析")
    add_text(
        final_note,
        "从测试结果看，ContentResolver 返回的歌曲名、时长和 URI 能直接驱动"
        "列表与 MediaPlayer；服务中的异步准备避免了播放大型音频时阻塞界面。"
        "Binder 同时承担命令入口和状态出口，Activity 只需根据 PlayerSnapshot "
        "渲染界面。SeekBar 拖动结束后由服务执行实际跳转，避免 Activity 直接"
        "操作播放器。通知栏仍由同一服务更新，因此切歌、暂停和停止时界面与"
        "通知状态保持一致。",
    )
    add_label(final_note, "4.2 评价与改进方向")
    add_text(
        final_note,
        "当前实现适合实验规模，功能完整且结构清楚。仍可在后续版本中加入"
        "MediaSession 和系统媒体控制中心、播放模式、收藏与歌单、专辑封面读取、"
        "以及播放状态持久化。对于真实手机，还可以增加耳机拔出暂停、音频焦点"
        "处理和来电中断恢复，使播放生命周期更稳健。",
    )
    final_note.paragraph_format.line_spacing = 1.1
    for run in final_note.runs:
        run.font.size = Pt(9.5)

    # Keep the supplied final grading note intact and separated from analysis text.
    final_note.paragraph_format.space_before = Pt(4)

    # The supplied template uses two anchored line images as decoration.
    for drawing_property in document.element.xpath(".//wp:docPr"):
        if not drawing_property.get("descr") and not drawing_property.get("title"):
            drawing_property.set("descr", "实验报告模板装饰性分隔线")
            drawing_property.set("title", "实验报告模板装饰性分隔线")

    document.save(OUTPUT_DOCX)
    print(OUTPUT_DOCX)


if __name__ == "__main__":
    main()
