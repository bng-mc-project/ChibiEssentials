# ChibiEssentials

Серверный essentials-мод для **Minecraft 1.20.1** на **Fabric** и **Forge**.  
Клиент не требуется — все сообщения и локализация обрабатываются на сервере.

## Зависимости

| Платформа | Зависимость | Версия |
|-----------|-------------|--------|
| Общее | [Architectury API](https://modrinth.com/mod/architectury-api) | `>= 9.2.14` |
| Общее | Minecraft | `1.20.1` |
| Общее | Java | `17+` |
| Fabric | Fabric Loader | `>= 0.19.3` |
| Fabric | Fabric API | `0.92.9+1.20.1` |
| Fabric | [fabric-permissions-api](https://modrinth.com/mod/fabric-permissions-api) (LuckPerms и др.) | `0.3.3` (встроен в jar) |
| Forge | Forge | `>= 47.4.10` |

Сборка через [Architectury Loom](https://docs.architectury.dev/loom/introduction).

```bash
./gradlew build
```

Готовые jar: `fabric/build/libs/` и `forge/build/libs/`.

---

## Возможности

- **Телепорты** — дома, спавн, варпы, `/back` с историей позиций
- **TPA** — запросы на телепорт с кнопками «Принять / Отклонить»
- **Личные сообщения** — `/msg`, `/m`, `/reply` с настраиваемым форматом
- **Админ-команды** — полёт, god, исцеление, vanish, gamemode, invsee, ec, head, whois
- **Warmup и cooldown** — задержка перед телепортом и кулдаун после (настраивается в конфиге и через пермишены)
- **Локализация из конфига** — тексты хранятся на сервере, клиентские lang-файлы не нужны
- **Пермишены** — интеграция с LuckPerms (Fabric) и Forge Permission API

---

## Конфигурация

При первом запуске создаётся папка `config/chibiessentials/` с дефолтными файлами.

При загрузке проверяется полная структура `config.json`. Если не хватает полей, JSON повреждён или файл пустой:
- текущий конфиг сохраняется как `config.1.bkp`, `config.2.bkp` и т.д.;
- записывается новый `config.json` из шаблона мода.

```
config/chibiessentials/
  config.json       — настройки мода
  lang/
    en_us.json      — английские тексты
    ru_ru.json      — русские тексты
```

### config.json

| Параметр | Описание |
|----------|----------|
| `language` | Язык сообщений: `en_us`, `ru_ru` и т.д. (файл из `lang/`) |
| `homes.defaultMax` | Лимит домов по умолчанию |
| `homes.warmup` / `homes.cooldown` | Warmup и cooldown для `/home` (сек.) |
| `back.maxHistory` | Глубина истории для `/back` |
| `back.warmup` / `back.cooldown` | Warmup и cooldown для `/back` |
| `back.onDeathOnly` | Записывать позицию в историю только при смерти |
| `spawn.warmup` / `spawn.cooldown` | Warmup и cooldown для `/spawn` |
| `warp.warmup` / `warp.cooldown` | Warmup и cooldown для `/warp` |
| `tpa.warmup` / `tpa.cooldown` | Warmup и cooldown после принятия TPA |
| `tpa.requestTimeoutSeconds` | Время жизни TPA-запроса |
| `messages.format` | Формат ЛС (`{sender}`, `{message}`, цвета через `&`) |
| `vanish.hideFromTab` | Скрывать vanished-игроков из таба |
| `vanish.hideChat` | Скрывать сообщения vanished-игроков в чате |

Перезагрузка: `/chibireload`, `/cereload` или `/essentialsreload`.

Тексты сообщений редактируются в `lang/<язык>.json`. При обновлении мода добавляйте новые ключи в существующие lang-файлы вручную (или скопируйте из jar).

---

## Команды

### Телепорты

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/back` | Вернуться на предыдущую позицию | `chibiessentials.back` |
| `/spawn` | Телепорт на спавн | `chibiessentials.spawn` |
| `/setspawn` | Установить точку спавна | `chibiessentials.setspawn` |

### Дома

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/sethome [имя]` | Сохранить дом | `chibiessentials.sethome` |
| `/home [имя]` | Телепорт домой | `chibiessentials.home` |
| `/delhome <имя>` | Удалить дом (при лимите > 1) | `chibiessentials.sethome` |
| `/listhomes` | Список домов (при лимите > 1) | `chibiessentials.home` |

При лимите домов = 1 имя не указывается (используется `home`).

### Варпы

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/warp <имя>` | Телепорт на варп | `chibiessentials.warp` или `chibiessentials.warp.<имя>` |
| `/warp create <имя>` | Создать / обновить варп | `chibiessentials.warp.create` |
| `/warp delete <имя>` | Удалить варп | `chibiessentials.warp.delete` |
| `/warp list` | Список варпов | `chibiessentials.warp.list` |

### TPA

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/tpa <игрок>` | Запрос: телепорт к игроку | `chibiessentials.tpa` |
| `/tpahere <игрок>` | Запрос: телепорт игрока к себе | `chibiessentials.tpa` |
| `/tpaccept [id]` | Принять запрос | `chibiessentials.tpaccept` |
| `/tpdeny [id]` | Отклонить запрос | `chibiessentials.tpdeny` |
| `/tpacancel` | Отменить свои исходящие запросы | `chibiessentials.tpacancel` |

### Сообщения

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/msg <игрок> <текст>` | Личное сообщение | `chibiessentials.msg` |
| `/m <игрок> <текст>` | Алиас `/msg` | `chibiessentials.msg` |
| `/reply <текст>` | Ответ последнему собеседнику | `chibiessentials.reply` |

### Читы и утилиты

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/fly [игрок]` | Переключить полёт в survival (не влияет на creative/spectator) | `chibiessentials.fly` |
| `/god [игрок]` | Переключить неуязвимость — отмена урона, без изменения gamemode | `chibiessentials.god` |
| `/heal [игрок]` | Полное исцеление | `chibiessentials.heal` |
| `/invsee <игрок>` | Просмотр инвентаря игрока | `chibiessentials.invsee` |
| `/vanish [игрок]` | Режим невидимости (spectator) | `chibiessentials.vanish` |
| `/ec [игрок]` | Открыть эндер-сундук | `chibiessentials.ec` / `chibiessentials.ec.others` |
| `/head <игрок>` | Выдать голову игрока | `chibiessentials.head` |

### Gamemode

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/gm <0-3> [игрок]` | Сменить режим (0=survival, 1=creative, 2=adventure, 3=spectator) | зависит от режима |
| `/gmc [игрок]` | Creative | `chibiessentials.gamemode.creative` |
| `/gms [игрок]` | Survival | `chibiessentials.gamemode.survival` |
| `/gmsp [игрок]` | Spectator | `chibiessentials.gamemode.spectator` |

Для `/gm 2` (adventure): `chibiessentials.gamemode.adventure`.

### Администрирование

| Команда | Описание | Пермишен |
|---------|----------|----------|
| `/whois <игрок>` | Показать IP игрока (кликабельная ссылка на 2ip.ru) | `chibiessentials.whois` |
| `/chibireload` | Перезагрузить конфиг и локали | `chibiessentials.reload` |
| `/cereload` | Алиас reload | `chibiessentials.reload` |
| `/essentialsreload` | Алиас reload | `chibiessentials.reload` |

---

## Пермишены

Все ноды имеют префикс **`chibiessentials.`**.  
Если пермишен-плагин не установлен, используется fallback по уровню OP (указан в скобках).

### Основные

| Пермишен | Описание | OP fallback |
|----------|----------|-------------|
| `chibiessentials.back` | `/back` | 0 |
| `chibiessentials.home` | `/home`, `/listhomes` | 0 |
| `chibiessentials.sethome` | `/sethome`, `/delhome` | 0 |
| `chibiessentials.spawn` | `/spawn` | 0 |
| `chibiessentials.setspawn` | `/setspawn` | 2 |
| `chibiessentials.warp` | Доступ ко всем варпам | 0 |
| `chibiessentials.warp.<имя>` | Доступ к конкретному варпу | 0 |
| `chibiessentials.warp.create` | `/warp create` | 2 |
| `chibiessentials.warp.delete` | `/warp delete` | 2 |
| `chibiessentials.warp.list` | `/warp list` | 0 |
| `chibiessentials.tpa` | `/tpa`, `/tpahere` | 0 |
| `chibiessentials.tpaccept` | `/tpaccept` | 0 |
| `chibiessentials.tpdeny` | `/tpdeny` | 0 |
| `chibiessentials.tpacancel` | `/tpacancel` | 0 |
| `chibiessentials.msg` | `/msg`, `/m` | 0 |
| `chibiessentials.reply` | `/reply` | 0 |
| `chibiessentials.ec` | `/ec` (свой сундук) | 0 |
| `chibiessentials.ec.others` | `/ec <игрок>` | 2 |
| `chibiessentials.head` | `/head` | 2 |
| `chibiessentials.fly` | `/fly` | 2 |
| `chibiessentials.god` | `/god` | 2 |
| `chibiessentials.heal` | `/heal` | 2 |
| `chibiessentials.invsee` | `/invsee` | 2 |
| `chibiessentials.vanish` | `/vanish` | 2 |
| `chibiessentials.vanish.see` | Видеть vanished-игроков | 2 |
| `chibiessentials.whois` | `/whois` | 2 |
| `chibiessentials.reload` | Перезагрузка конфига | 2 |
| `chibiessentials.gamemode.creative` | Creative | 2 |
| `chibiessentials.gamemode.survival` | Survival | 2 |
| `chibiessentials.gamemode.adventure` | Adventure | 2 |
| `chibiessentials.gamemode.spectator` | Spectator | 2 |

### Мета-пермишены (числовые значения)

Переопределяют значения из конфига для конкретного игрока.

| Пермишен | Описание |
|----------|----------|
| `chibiessentials.home.max` | Максимум домов |
| `chibiessentials.back.max` | Глубина истории `/back` |
| `chibiessentials.back.cooldown` | Cooldown `/back` (сек.) |
| `chibiessentials.back.warmup` | Warmup `/back` (сек.) |
| `chibiessentials.spawn.cooldown` | Cooldown `/spawn` |
| `chibiessentials.spawn.warmup` | Warmup `/spawn` |
| `chibiessentials.home.cooldown` | Cooldown `/home` |
| `chibiessentials.home.warmup` | Warmup `/home` |
| `chibiessentials.warp.cooldown` | Cooldown `/warp` |
| `chibiessentials.warp.warmup` | Warmup `/warp` |
| `chibiessentials.tpa.cooldown` | Cooldown TPA |
| `chibiessentials.tpa.warmup` | Warmup TPA |

### Пример (LuckPerms)

```
/lp group default permission set chibiessentials.home true
/lp group default permission set chibiessentials.spawn true
/lp group default permission set chibiessentials.back true
/lp group default permission set chibiessentials.tpa true
/lp group vip permission set chibiessentials.home.max 3
/lp group vip permission set chibiessentials.warp.shop true
```

---

## Лицензия

MIT
