# AstraxTitles 1.1.0 - Danh hiệu nhiệm vụ cho BoxMine RPG

Danh hiệu mở khóa bằng nhiệm vụ, mỗi danh hiệu có **buff** và **nội tại** riêng, hiển thị **trước tên trong Tab** (chữ in hoa nhỏ). Paper 1.21.4, Java 21.

> **Trạng thái:** logic (nhiệm vụ, tiến độ, mô tả nội tại) đã chạy 14 test đạt; phần Bukkit (nội tại chiến đấu, GUI, hook plugin khác) **chưa build bằng Maven và chưa chạy trên server thật**. Hãy thử trên server test.

## Tích hợp với 2 plugin của bạn (hai plugin KHÁC NHAU)
| Plugin | Cách đọc | Nhiệm vụ trong titles.yml |
|---|---|---|
| **AstraxElementer** (`/elementer`) | reflection, chỉ đọc: `ElementerCore.elementService.get(uuid).hasUnlocked(...)`; boss nhận diện qua khóa PDC `astraxelementer:boss_id` | `ELEMENTER_UNLOCK` (filter: FIRE ICE LIGHTNING EARTH WIND LIGHT DARK DRAGON), `BOSS_KILL` (filter: id boss như `dragon_lord`) |
| **NguyenTo** (`/nguyento`) | đọc PDC người chơi `nguyento:owned` | `NGUYENTO_UNLOCK` (filter: lua nuoc dat gio set bang bong_toi anh_sang) |

Không sửa code của hai plugin kia. Nếu bạn đổi tên field `elementService` trong `ElementerCore`, cầu nối tự tắt và ghi cảnh báo; cách bền hơn là thêm vào `ElementerCore` một getter `public ElementService elementService()` (bản hiện tại chưa cần).
Kiểm tra kết nối: `/titleadmin check <tên>`.

## Cách mở khóa và thông báo
Mỗi danh hiệu có danh sách `requirements`; phải hoàn thành **tất cả**. Khi đủ, người chơi nhận: tin nhắn, title lớn giữa màn hình, âm thanh, và thông báo toàn server nếu danh hiệu Huyền Thoại trở lên (hoặc `announce: true`). Danh hiệu đầu tiên mở được tự động đeo.
Nhiệm vụ "mở khóa nguyên tố" được kiểm tra mỗi `poll-seconds` (mặc định 5 giây) và ghi nhớ; nên mở khóa xong sẽ có thông báo trong vài giây. Nhiệm vụ đếm (kill, online...) cập nhật tức thì.

## 13 danh hiệu mẫu
| # | Danh hiệu | Nhiệm vụ | Buff | Nội tại |
|---|---|---|---|---|
| 1 | Tân Thủ | Online 15 phút | đào +1, đánh +0.15, +1 máu | - |
| 2 | Chiến Binh | Hạ 10 người | +1 dame, +2 máu, đánh +0.20 | - |
| 3 | Cuồng Chiến | Hạ 40 người | +1.25 dame, +3 máu, đánh +0.30 | - |
| 4 | Đồ Sát | Hạ 100 người | +2 dame, +5 máu, đánh +0.40 | - |
| 5 | Hỏa Viễn | Elementer Hỏa + NguyenTo Lửa | +10% tốc độ, +1.5 dame, +3 máu | đòn đánh gây cháy; +20% dame lên mục tiêu đang cháy |
| 6 | Thủy Hộ | Elementer Băng + NguyenTo Nước | +20% tốc độ, +5 máu | kháng lửa 50%; +10% hồi máu |
| 7 | Địa Thổ | NguyenTo Đất + Elementer Thổ | +5 giáp, +6 máu | giảm 10% sát thương nhận |
| 8 | Lôi Đế | NguyenTo Sét + Elementer Lôi | +1.5 dame, đánh +0.4 | 10% giáng sét |
| 9 | Phong Nhã | NguyenTo Gió + Elementer Phong | +20% tốc độ, đánh +0.5 | 20% né tránh |
| 10 | Băng Quang | NguyenTo Băng + Elementer Quang | +6 máu | +20% hồi máu; 20% gây lạnh |
| 11 | Long Thần | Hạ boss Long Vương Hủy Diệt + Elementer Rồng | +2.5 dame, +5 máu, đánh +0.5 | Long Uy / Long Tức / Long Hồn |
| 12 | Thánh Đế | NguyenTo Ánh Sáng | +2.5 dame, +20% tốc độ, +5 máu | Quang Kích / Thánh Quang / Quang Hồi |
| 13 | Bóng Tối | Elementer Bóng Tối + NguyenTo Bóng Tối + 100 kill | +3 dame, +8 máu, đánh +0.6, +10% tốc độ | Hắc Thực / Ám Ảnh / Tử Vực / Hắc Giới (+10% hút máu) |

## Các điểm mình đã diễn giải (sửa trong titles.yml nếu khác ý bạn)
- **"Tốc độ đào +1" (Tân Thủ):** áp đúng số bạn ghi = `block_break_speed +1` (đào nhanh gấp đôi). Nếu muốn nhẹ hơn, đổi thành `"20%"`.
- **HP:** `+1 máu` = 1 điểm máu (nửa tim), cùng thang với NguyenTo (2 máu = 1 tim).
- **Hỏa Viễn:** "đòn đánh gây cháy +20% dame" = đòn đánh luôn thiêu đốt 3s và **+20% sát thương lên mục tiêu đang cháy**.
- **Thủy Hộ:** "kháng lửa" = giảm 50% sát thương từ lửa/dung nham (chỉ khai báo một lần dù bạn ghi ở cả buff lẫn nội tại).
- **Bóng Tối:** "2 elementer + Nguyento" mình hiểu là **Elementer Bóng Tối + NguyenTo Bóng Tối + 100 kill**. Muốn khác, sửa mục `requirements` của `bong_toi`.
- **"Sát thương từ Bóng Tối / Quang Vương":** nhận diện theo nguyên tố Elementer đang dùng của kẻ tấn công hoặc boss Elementer (`dark_lord`, `light_paladin`... trong `boss-elements`). Chiêu của NguyenTo chưa nhận diện được.
- **Hạ boss:** tính cho người đánh đòn cuối **và** mọi người gây >= 5% máu boss (`boss-credit-percent`).
- **Chống farm:** hạ cùng một người chơi chỉ tính lại sau 120 giây (`anti-farm.same-victim-seconds`).

## Nội tại: tham số
`type` + `name` (tên hiển thị) + `value`, `value2` (%), `chance` (%), `threshold` (% máu), `duration`/`cooldown` (giây), `filter`.
Loại: `OUTGOING_DAMAGE_PERCENT`, `DAMAGE_VS_BURNING_PERCENT`, `BOSS_DAMAGE_PERCENT`, `IGNITE_ON_HIT`, `SLOW_ON_HIT`, `WEAKEN_ON_HIT`, `LIFESTEAL_PERCENT`, `LIFESTEAL_ON_HIT`, `LIGHTNING_ON_HIT`, `BONUS_DAMAGE_ON_HIT` (filter FIRE/LIGHT/DARK chọn hiệu ứng hạt, `duration` > 0 thì thiêu đốt), `DODGE`, `DAMAGE_REDUCTION_PERCENT` (filter FIRE hoặc danh sách DamageCause), `DAMAGE_REDUCTION_FROM_ELEMENT`, `HEAL_RECEIVED_PERCENT`, `LOW_HP_BOOST`, `LOW_HP_HEAL`.
Nội tại chỉ áp cho **đòn cận chiến** (và nhận sát thương); tổng giảm sát thương bị chặn ở `max-damage-reduction` (80%).

## Tương tác với NguyenTo
NguyenTo đặt giá trị gốc (base) của máu/sát thương/tốc độ; AstraxTitles cộng bằng modifier tạm thời nên **cộng dồn, không ghi đè nhau**. Riêng "giáp %" của NguyenTo tính theo giáp mặc, nên giáp +5 của Địa Thổ có thể bị nhân nhẹ. Thoát game: buff của titles được gỡ, vào lại tự áp.

## Lệnh và quyền
`/title` (menu), `/title equip <id>`, `/title off`, `/title list` - `astraxtitles.use`.
`/titleadmin reload | list | give <p> <id> | take <p> <id> | progress <p> <id> <nhiệm vụ#> <số> | check <p>` - `astraxtitles.admin`.

## Hiển thị
Tab: `✦ ᴛᴀɴ ᴛʜᴜ | Steve` (rank của plugin khác nằm sau tên). Nếu dùng plugin quản lý Tab (TAB...) thì `tab.enabled: false` và đặt `%astraxtitles_prefix%` vào tabprefix. Chat mặc định tắt (`chat.enabled`). PlaceholderAPI: `%astraxtitles_prefix%`, `_title`, `_id`, `_count`.

## Kiểm thử thủ công nên làm
- [ ] Mở khóa Elementer Hỏa + NguyenTo Lửa (`/nguyento unlock <tên> lua`, `/elementeradmin give <tên> voucher fire`...): trong ~5s có thông báo "Mở khóa danh hiệu Hỏa Viễn".
- [ ] Chỉ có một trong hai plugin nguyên tố: KHÔNG mở khóa.
- [ ] `/elementeradmin boss spawn dragon_lord` rồi hạ boss: tiến độ Long Thần.
- [ ] Hạ 10 người chơi (2 tài khoản): hạ lại cùng người trong 120s không tính thêm.
- [ ] Đeo Phong Nhã: thấy "Né tránh!" ngẫu nhiên; đeo Thủy Hộ: đứng trong lửa nhận ít sát thương hơn.
- [ ] Long Hồn / Quang Hồi / Tử Vực: máu dưới ngưỡng kích hoạt đúng một lần mỗi 30s.
- [ ] Đổi danh hiệu liên tục: không kẹt buff tốc độ/máu; thoát vào lại: buff còn đúng.
