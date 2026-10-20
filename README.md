# AstraxTitles 1.0.0 — Flat build-ready source

Plugin danh hiệu và thành tựu cho **Paper 1.21.4 / Java 21**.

## Build trên GitHub Actions

1. Tạo repository GitHub mới.
2. Giải nén ZIP này.
3. Upload **toàn bộ nội dung bên trong ZIP** vào thư mục gốc repository (đừng upload cả thư mục ZIP bọc bên ngoài).
4. Vào tab **Actions**. Workflow `Build AstraxTitles` tự chạy khi push hoặc pull request.
5. Khi build thành công, mở lần chạy workflow → phần **Artifacts** → tải `AstraxTitles-plugin`.

## Build trên máy tính

Yêu cầu JDK 21 và Maven:

```bash
mvn clean package
```

JAR đầu ra:

```text
target/AstraxTitles-1.0.0.jar
```

Chép JAR vào thư mục `plugins/` của server Paper 1.21.4. PlaceholderAPI là tùy chọn; nếu dùng placeholder thì cài PlaceholderAPI vào server.

## Bố cục phẳng

Các file `.java`, `plugin.yml`, `config.yml`, `titles.yml` và `pom.xml` nằm ngay thư mục gốc. Các file Java vẫn giữ khai báo `package` gốc; Maven được cấu hình để biên dịch từ thư mục gốc.

## Lệnh

- `/title` — mở menu danh hiệu.
- `/title equip <id>` — đeo danh hiệu đã mở khóa.
- `/title off` — tháo danh hiệu.
- `/title list` — xem danh hiệu.
- `/titleadmin reload|list|give|take|progress` — quản trị.

Quyền quản trị: `astraxtitles.admin`.
