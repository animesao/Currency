# Настройка базы данных для CurrencyPlugin

## 📦 Типы хранилищ

Плагин поддерживает три типа хранилищ данных:

1. **YAML** - файловое хранилище (по умолчанию)
2. **SQLite** - локальная база данных
3. **MySQL** - удаленная база данных

## ⚙️ Конфигурация

### YAML (Файловое хранилище)

Самый простой вариант, не требует настройки базы данных.

```yaml
storage:
  type: YAML
  yaml:
    folder: "playerdata/"
    auto-save-interval: 5
```

**Преимущества:**
- Не требует установки базы данных
- Простая настройка
- Легко редактировать вручную

**Недостатки:**
- Медленнее при большом количестве игроков
- Не подходит для нескольких серверов

---

### SQLite (Локальная база данных)

Хороший баланс между простотой и производительностью.

```yaml
storage:
  type: SQLite
  sqlite:
    filename: "currency.db"
    path: "data/"
```

**Преимущества:**
- Быстрее чем YAML
- Не требует внешнего сервера БД
- Автоматическое создание базы

**Недостатки:**
- Не подходит для нескольких серверов
- Сложнее редактировать вручную

---

### MySQL (Удаленная база данных)

Лучший вариант для больших серверов и сетей.

```yaml
storage:
  type: MySQL
  mysql:
    host: "localhost"
    port: 3306
    database: "minecraft"
    username: "root"
    password: "your_password"
    properties:
      useSSL: false
      autoReconnect: true
      characterEncoding: "utf8"
      maxReconnects: 3
    pool:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Преимущества:**
- Максимальная производительность
- Поддержка нескольких серверов (BungeeCord/Velocity)
- Централизованное хранение данных
- Резервное копирование

**Недостатки:**
- Требует установки MySQL сервера
- Сложнее в настройке

---

## 🔧 Установка MySQL

### Windows

1. Скачайте MySQL с официального сайта: https://dev.mysql.com/downloads/installer/
2. Запустите установщик и выберите "MySQL Server"
3. Следуйте инструкциям установщика
4. Запомните пароль root пользователя

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

### Linux (CentOS/RHEL)

```bash
sudo yum install mysql-server
sudo systemctl start mysqld
sudo mysql_secure_installation
```

---

## 🗄️ Создание базы данных

### Вариант 1: Через командную строку

```bash
mysql -u root -p
```

Затем выполните SQL команды:

```sql
CREATE DATABASE minecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'minecraft'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Вариант 2: Через phpMyAdmin

1. Откройте phpMyAdmin
2. Перейдите в раздел "Базы данных"
3. Создайте новую базу с именем `minecraft`
4. Выберите кодировку `utf8mb4_unicode_ci`
5. Создайте пользователя в разделе "Привилегии"

---

## 🔐 Настройка удаленного доступа (для BungeeCord)

Если MySQL на другом сервере, разрешите удаленные подключения:

### 1. Отредактируйте конфиг MySQL

```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

Найдите строку:
```
bind-address = 127.0.0.1
```

Замените на:
```
bind-address = 0.0.0.0
```

### 2. Создайте пользователя с удаленным доступом

```sql
CREATE USER 'minecraft'@'%' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'%';
FLUSH PRIVILEGES;
```

### 3. Перезапустите MySQL

```bash
sudo systemctl restart mysql
```

### 4. Откройте порт в файрволе

```bash
sudo ufw allow 3306/tcp
```

---

## 📊 Структура таблиц

Плагин автоматически создаст следующую таблицу:

```sql
CREATE TABLE currency_balances (
    player_uuid VARCHAR(36) NOT NULL,
    currency_id VARCHAR(64) NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (player_uuid, currency_id),
    INDEX idx_player (player_uuid),
    INDEX idx_currency (currency_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🔄 Миграция данных

### Из YAML в MySQL/SQLite

1. Остановите сервер
2. Сделайте резервную копию папки `playerdata/`
3. Измените `storage.type` в config.yml
4. Запустите сервер (создастся новая база)
5. Используйте скрипт миграции (см. ниже)

### Скрипт миграции (пример)

```java
// Добавьте команду в плагин для миграции
// /currency migrate yaml-to-mysql
```

---

## 🛡️ Безопасность

### Рекомендации:

1. **Используйте сильные пароли**
   ```
   Плохо: password123
   Хорошо: mK9$pL2#vN8@qR5
   ```

2. **Создайте отдельного пользователя**
   - Не используйте root для подключения
   - Дайте только необходимые права

3. **Используйте SSL для удаленных подключений**
   ```yaml
   properties:
     useSSL: true
     requireSSL: true
   ```

4. **Регулярно делайте бэкапы**
   ```bash
   mysqldump -u minecraft -p minecraft > backup.sql
   ```

---

## 🐛 Решение проблем

### Ошибка: "Access denied for user"

**Решение:**
```sql
GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
FLUSH PRIVILEGES;
```

### Ошибка: "Unknown database 'minecraft'"

**Решение:**
```sql
CREATE DATABASE minecraft;
```

### Ошибка: "Can't connect to MySQL server"

**Проверьте:**
1. Запущен ли MySQL: `sudo systemctl status mysql`
2. Правильный ли хост и порт в config.yml
3. Открыт ли порт в файрволе

### Медленная работа

**Оптимизация:**
1. Увеличьте `maximum-pool-size` в конфиге
2. Добавьте индексы в базу данных
3. Используйте SSD для хранения базы

---

## 📈 Производительность

### Сравнение типов хранилищ:

| Тип | Скорость чтения | Скорость записи | Многосерверность |
|-----|----------------|-----------------|------------------|
| YAML | ⭐⭐ | ⭐⭐ | ❌ |
| SQLite | ⭐⭐⭐⭐ | ⭐⭐⭐ | ❌ |
| MySQL | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ |

### Рекомендации по выбору:

- **< 50 игроков онлайн**: YAML или SQLite
- **50-200 игроков**: SQLite или MySQL
- **> 200 игроков**: MySQL
- **BungeeCord сеть**: Только MySQL

---

## 📞 Поддержка

Если возникли проблемы:
1. Проверьте логи сервера
2. Убедитесь что база данных создана
3. Проверьте права пользователя MySQL
4. Проверьте подключение: `telnet localhost 3306`

---

**Версия:** 1.0.0  
**Последнее обновление:** 2026
