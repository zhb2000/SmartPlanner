# 数据库
## TodoDatabase.db
待办事项数据库

### TodoList
建表语句

```SQL
CREATE TABLE TodoList (
    id INTEGER PRIMARY KEY AUTOINCREMENT, --id，主键（自增）
    title TEXT NOT NULL, --标题
    is_complete INTEGER NOT NULL, --是否完成，0为假，1为真
    is_star INTEGER NOT NULL, --是否加星，0为假，1为真
    alarm TEXT, --闹钟提醒时间，格式：yyyy-MM-dd HH:mm:ss
    note TEXT, --备注
    tag TEXT, --标签
    end_date TEXT, --截止日期，格式：yyyy-MM-dd
    create_time TEXT NOT NULL, --创建时间，格式:yyyy-MM-dd HH:mm:ss
    edit_time TEXT, --上次编辑时间，格式：yyyy-MM-dd HH:mm:ss
    complete_time TEXT --完成时间，格式：yyyy-MM-dd HH:mm:ss
    );
```

### TodoTag
建表语句

```SQL
CREATE TABLE TodoTag (
    id INTEGER NOT NULL, --id
    tag TEXT NOT NULL --标签
    );
```

## TomatoDatabase.db
番茄钟数据库

### TomatoHistory
建表语句

```SQL
CREATE TABLE TomatoHistory (
    id INTEGER PRIMARY KEY AUTOINCREMENT, --id，主键（自增）
    title TEXT NOT NULL, --标题
    is_successful INTEGER NOT NULL, --是否成功，0为假，1为真
    time_sum INTEGER NOT NULL, --总时长（分钟）
    work_sum INTEGER NOT NULL, --工作总时长（分钟）
    rest_sum INTEGER NOT NULL, --休息总时长（分钟）
    work_len INTEGER NOT NULL, --单个工作时长（分钟）
    rest_len INTEGER NOT NULL, --单个休息时长（分钟）
    clock_cnt INTEGER NOT NULL, --计划的重复次数
    start_time TEXT NOT NULL, --开始时间，格式：yyyy-MM-dd HH:mm:ss
    end_time TEXT NOT NULL --结束时间，格式：yyyy-MM-dd HH:mm:ss
    );
```