# Currency Plugin

Профессиональный плагин для Minecraft с поддержкой неограниченного количества кастомных валют. Каждая валюта имеет свою команду, placeholder и полную настройку через конфиг.

## ✨ Особенности

- 🎨 **Поддержка HEX и RGB цветов** - используйте `#FF5555` или `<rgb(255,85,85)>` в названиях и сообщениях
- 💰 **Неограниченное количество валют** - добавляйте столько валют, сколько нужно
- 🔧 **Кастомные команды** - для каждой валюты своя команда, настраиваемая в конфиге
- 📊 **PlaceholderAPI интеграция** - используйте `%currency_<placeholder>%` в других плагинах
- ⌨️ **TAB-completion** - полная поддержка автодополнения команд
- 💾 **Множественные типы хранилищ** - YAML, SQLite или MySQL
- 🌐 **Поддержка BungeeCord/Velocity** - синхронизация данных через MySQL
- 🔐 **Система прав** - настраиваемые permissions для каждой команды
- ⚡ **Высокая производительность** - HikariCP пул соединений для MySQL
- 🔄 **Асинхронные операции** - не блокирует основной поток сервера

## 📦 Требования

- Minecraft 1.16+
- Java 17+
- PlaceholderAPI (опционально, для placeholder'ов)

## 🚀 Установка

1. Скачайте `CurrencyPlugin-2.0.0.jar` из папки `target/`
2. Поместите файл в папку `plugins/` вашего сервера
3. Перезапустите сервер
4. Настройте валюты в `plugins/CurrencyPlugin/config.yml`
5. (Опционально) Настройте MySQL для многосерверной синхронизации

## 💾 Типы хранилищ

Плагин поддерживает три типа хранения данных:

### YAML (по умолчанию)
- Простая настройка
- Подходит для небольших серверов (< 50 игроков)
- Данные хранятся в файлах

### SQLite
- Локальная база данных
- Быстрее чем YAML
- Подходит для средних серверов (50-200 игроков)

### MySQL
- Удаленная база данных
- Максимальная производительность
- **Поддержка BungeeCord/Velocity сетей**
- Подходит для больших серверов (200+ игроков)

📖 **Подробная инструкция по настройке:** см. [DATABASE_SETUP.md](DATABASE_SETUP.md)

## ⚙️ Конфигурация

### Настройка хранилища

```yaml
storage:
  # Тип хранилища: YAML, SQLite, MySQL
  type: SQLite
  
  # Настройки MySQL (для BungeeCord сетей)
  mysql:
    host: "localhost"
    port: 3306
    database: "minecraft"
    username: "root"
    password: "password"
    pool:
      maximum-pool-size: 10
      minimum-idle: 2
```

### Добавление новой валюты

Откройте `config.yml` и добавьте новую валюту:

```yaml
currencies:
  my_currency:  # ID валюты
    display-name: "#00FF00Моя Валюта"  # HEX/RGB/&c цвета
    command: "mycurrency"              # Команда: /mycurrency
    placeholder: "mycurrency"          # Placeholder: %currency_mycurrency%
    starting-balance: 100.0            # Стартовый баланс
    
    permissions:
      view: "currency.mycurrency.view"
      give: "currency.mycurrency.give"
      take: "currency.mycurrency.take"
      pay: "currency.mycurrency.pay"
      set: "currency.mycurrency.set"
    
    messages:
      balance: "#00FF00Ваш баланс: {amount} монет"
      # ... другие сообщения
```

### Примеры цветов

```yaml
# Стандартные цвета Minecraft
display-name: "&c&lКрасный"

# HEX цвета
display-name: "#FF5555Красный"

# RGB цвета
display-name: "<rgb(255,85,85)>Красный"
```

## 📝 Команды

Каждая валюта имеет свою команду (настраивается в конфиге). Пример для валюты с командой `/coins`:

### Основные команды

- `/coins` - посмотреть свой баланс
- `/coins balance [игрок]` - посмотреть баланс игрока
- `/coins give <игрок> <сумма>` - выдать валюту игроку (админ)
- `/coins take <игрок> <сумма>` - забрать валюту у игрока (админ)
- `/coins pay <игрок> <сумма>` - передать валюту другому игроку
- `/coins set <игрок> <сумма>` - установить баланс игрока (админ)

## 🔑 Права (Permissions)

Права настраиваются индивидуально для каждой валюты в конфиге:

```yaml
permissions:
  view: "currency.coins.view"      # Просмотр баланса
  give: "currency.coins.give"      # Выдача (админ)
  take: "currency.coins.take"      # Изъятие (админ)
  pay: "currency.coins.pay"        # Передача игрокам
  set: "currency.coins.set"        # Установка (админ)
```

## 📊 PlaceholderAPI

Используйте placeholder'ы в других плагинах:

- `%currency_<placeholder>%` - показывает баланс игрока

Примеры:
- `%currency_coins%` - баланс монет
- `%currency_crystals%` - баланс кристаллов
- `%currency_rubies%` - баланс рубинов

## 🔨 Компиляция

```bash
mvn clean package
```

Готовый плагин будет в папке `target/CurrencyPlugin-1.0.0.jar`

## 📁 Структура файлов

```
CurrencyPlugin/
├── config.yml              # Конфигурация валют и хранилища
├── playerdata/             # Данные игроков (только для YAML)
│   ├── <uuid>.yml
│   └── ...
├── data/                   # База данных SQLite
│   └── currency.db
└── backups/                # Резервные копии (если включено)
    └── ...
```

## 💡 Примеры использования

### Пример 1: Создание валюты "Алмазы"

```yaml
diamonds:
  display-name: "#00FFFFАлмазы"
  command: "diamonds"
  placeholder: "diamonds"
  starting-balance: 0.0
  permissions:
    view: "currency.diamonds.view"
    give: "currency.diamonds.give"
    take: "currency.diamonds.take"
    pay: "currency.diamonds.pay"
    set: "currency.diamonds.set"
  messages:
    balance: "#00FFFFВаш баланс: {amount} алмазов"
    # ...
```

### Пример 2: Использование в других плагинах

С помощью PlaceholderAPI можно показать баланс в табе, скорборде и т.д.:

```
Баланс: %currency_coins% монет
Кристаллы: %currency_crystals%
```

## 🐛 Поддержка

Если у вас возникли проблемы:

1. Проверьте версию Java (требуется 17+)
2. Убедитесь, что PlaceholderAPI установлен (для placeholder'ов)
3. Проверьте конфигурацию в `config.yml`
4. Посмотрите логи сервера на наличие ошибок
5. Для проблем с MySQL см. [DATABASE_SETUP.md](DATABASE_SETUP.md)

## 🌐 BungeeCord/Velocity сети

Для синхронизации данных между серверами:

1. Установите MySQL сервер
2. Настройте одинаковые параметры MySQL на всех серверах
3. Установите `storage.type: MySQL` в config.yml
4. Перезапустите все серверы

Подробнее: [DATABASE_SETUP.md](DATABASE_SETUP.md)

## 📊 Производительность

| Игроков онлайн | Рекомендуемое хранилище |
|----------------|-------------------------|
| < 50 | YAML или SQLite |
| 50-200 | SQLite или MySQL |
| 200+ | MySQL |
| BungeeCord сеть | MySQL (обязательно) |

---

**Версия:** 2.0.0  
**Minecraft:** 1.16+  
**Java:** 17+  
**Автор:** animesao
