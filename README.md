### Инструкция по запуску

1. **Клонируйте репозиторий**

2. **Создайте базу данных**
   Использовал PostgreSQL, но можно использовать и другую базу данных.

3. **Создайте необходимые таблицы:**
   SQL код для создания таблиц находится в файле `schema.sql`!

4. **Заполните значения в `application.properties`:**
   - Можете удалить расширение `.origin` у `application.properties.origin` и работать с этим шаблоном.
   - Пример заполнения значений для подключения базы данных:
     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/translation_db
     spring.datasource.username=postgres
     spring.datasource.password=password
     spring.datasource.driver-class-name=org.postgresql.Driver
     ```

5. **Действия для получения следующих двух значений:**
   - `yandex.translate.api-key=`
   - `yandex.translate.folder-id=`

   **1 Вариант**  
   Следовать этим инструкциям (Легче выбрать консоль управления):
   - [Получение API ключа](https://yandex.cloud/ru/docs/translate/operations/sa-api-key#prepare-cloud)
   - [Получение ID папки](https://yandex.cloud/ru/docs/resource-manager/operations/folder/get-id#console_1)

   **2 Вариант (Быстрее и проще)**  
   Воспользоваться моими данными:
   ```properties
   yandex.translate.api-key=AQVN0YZ3RKFImWqqWVQ5Iu41M3HiCsgLh7ychAOE
   yandex.translate.folder-id=b1gsq4vfcf5ubhv2sk8b
   ```

   **Убедительная просьба:** Перевод текста не бесплатный, у моего аккаунта действует грант, из которых только 3000 рублей можно потратить на перевод текста. Поэтому прошу не переводить “Война и мир” на несколько языков, спасибо за понимание!

6. **Запустите приложение в вашем IDE и перейдите на:**  
   [http://localhost:8080/translator](http://localhost:8080/translator)

### О работе приложения:

- В выпадающих меню выберите интересующие вас языки, введите текст и нажмите на кнопку “Перевести”.
- При каждом запуске приложение запрашивает поддерживаемые языки. Это сделано, чтобы при самом первом запуске программы в базе данных были языки, и благодаря этому программа всегда имеет актуальный список поддерживаемых языков.
- Также есть возможность запросить языки самому по ссылке:  
  [http://localhost:8080/translator/get-supported-languages](http://localhost:8080/translator/get-supported-languages)
- При следующем включении приложение вспомнит языки вашего последнего перевода и включит их автоматически.
- Так как в API v2 сервиса Yandex Translate действуют следующие ограничения:
  ![image](https://github.com/user-attachments/assets/1f243cb9-b526-4a95-ab98-261f8cfac10d)
  - А по ТЗ надо делать запрос по каждому слову. Приходится усыплять потоки при большом введённом тексте, чтобы не было ошибки, из-за этого приложение может дольше выполнять перевод.
  - Также из-за этого есть ограничение в 200 слов, чтобы пользователь "не ждал тысячелетие", переводя очень огромный текст. Поэтому рекомендуется использовать приложение для небольших текстов, но при огромном желании можно поменять переменную `MAX_WORDS` в сервисе `TranslationService`.
- При отправке слишком большого количества слов пользователь увидит уведомление об этом.
