# Currency

# Предупреждение!!!!!!!!!! Эта версия плагина больше не поддерживаеться потому что была улучшенная версия плагина на [сайта](https://alfheimguide.spcfy.eu/plugins.php?plugin_id=31). Это ознакомительная версия о том что плагины безопасны.

Гибкий плагин для Minecraft с поддержкой неограниченного количества кастомных валют. Каждая валюта имеет свою команду, placeholder и полную настройку через конфиг.

## ✨ Особенности

- 🎨 **Поддержка HEX и RGB цветов** - используйте `#FF5555` или `<rgb(255,85,85)>` в названиях и сообщениях
- 💰 **Неограниченное количество валют** - добавляйте столько валют, сколько нужно
- 🔧 **Кастомные команды** - для каждой валюты своя команда, настраиваемая в конфиге
- 📊 **PlaceholderAPI интеграция** - используйте `%currency_<placeholder>%` в других плагинах
- ⌨️ **TAB-completion** - полная поддержка автодополнения команд
- 💾 **YAML хранилище** - данные игроков сохраняются в YAML файлах
- 🔐 **Система прав** - настраиваемые permissions для каждой команды

## 📦 Требования

- Minecraft 1.16+
- Java 17+
- PlaceholderAPI (опционально, для placeholder'ов)

## 🚀 Установка

1. Скачайте `CurrencyPlugin-1.0.0.jar` из папки `target/`
2. Поместите файл в папку `plugins/` вашего сервера
3. Перезапустите сервер
4. Настройте валюты в `plugins/CurrencyPlugin/config.yml`

## ⚙️ Конфигурация

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
├── config.yml              # Конфигурация валют
└── playerdata/            # Данные игроков
    ├── <uuid>.yml
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

---

## 🐛 Конец поддержки

Плагин поддерживаеться до 2026 года

---
 ([Wiki](https://alfheimguide.ct.ws/wiki_article?slug=currencyplugin))
 
**Версия:** 1.0.0  
**Minecraft:** 1.16+  
**Java:** 17+
