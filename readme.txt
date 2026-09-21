# AI TEAM — ПОЛНЫЙ КОНТЕКСТ ПРОЕКТА

## Промпт для продолжения разработки в новом чате

Ты продолжаешь разработку моего проекта **AI Team**. Не начинай проект заново и не предлагай абстрактную архитектуру с нуля. Сначала используй весь контекст ниже как исходное состояние проекта.

Общайся со мной **на русском языке**.

---

# 1. ЧТО ЭТО ЗА ПРОЕКТ

AI Team — моя собственная система мультиагентной разработки программного обеспечения.

Идея:

```text
Пользователь / Director
        ↓
    Architect
        ↓
     Senior
        ↓
     Middle (если нужен)
        ↓
     Tester
        ↓
   PASS / FAIL
        ↓
   Senior Fix
        ↓
     Tester
```

Это не игрушечный чат с несколькими персонажами.

Цель — сделать настоящую систему, в которой несколько AI-агентов работают как команда разработчиков:

* Architect — анализирует задачу и формирует архитектурный контракт.
* Senior — реализует задачу, работает с файлами, запускает сборку и тесты.
* Middle — подключается, если Senior определяет, что нужен дополнительный разработчик.
* Tester — проверяет результат и НЕ должен изменять проект.
* Senior Fix — исправляет подтверждённые Tester проблемы.
* MainApp — Director/оркестратор, управляющий всем pipeline.

---

# 2. МОЯ ПОЗИЦИЯ В ПРОЕКТЕ

Я сам разрабатываю этот проект.

Уровень моих знаний позволяет мне сделать его самостоятельно, но без AI-помощи многие архитектурные и технические моменты заняли бы значительно больше времени.

Поэтому:

* не относись ко мне как к человеку, который вообще не понимает код;
* объясняй причины архитектурных решений;
* не скрывай технические детали;
* если предлагаешь изменение — сначала проанализируй существующий код;
* не ломай существующую архитектуру без необходимости;
* не переписывай всё ради «красивой архитектуры»;
* предпочитай постепенное развитие;
* если меняем файл, я предпочитаю получать **полный готовый файл**, а не маленький patch;
* если возможно, сохраняй существующие комментарии и структуру.

---

# 3. ТЕХНОЛОГИЧЕСКИЙ СТЕК

Текущий проект:

* Java 21
* Maven
* LangChain4j 1.11.0
* LangChain4j OpenAI integration 1.11.0
* JavaFX 21.0.2
* DeepSeek через OpenAI-compatible API
* SLF4J
* IntelliJ IDEA
* Windows

Основной AI provider сейчас:

```text
DeepSeek
```

Текущая модель:

```text
deepseek-flash
```

LangChain4j:

```text
1.11.0
```

---

# 4. РАСПОЛОЖЕНИЕ ПРОЕКТА

AI Team находится примерно здесь:

```text
C:\Users\IgorS\Desktop\IITeam\Ai-team v79
```

Рабочая область, с которой агенты работают:

```text
C:\Users\IgorS\Desktop\IITeam
```

Сам AI Team защищён от изменения.

В AIEngine:

```java
public static final Path WORKSPACE_ROOT = Paths.get(
        System.getProperty("user.home"), "Desktop", "IITeam")
        .toAbsolutePath().normalize();
```

Защищённый корень определяется через:

```java
Paths.get(System.getProperty("user.dir"))
        .toAbsolutePath()
        .normalize();
```

Таким образом AI Team может создавать/изменять проекты в workspace, но не должен изменять сам себя.

---

# 5. ТЕКУЩАЯ АРХИТЕКТУРА

Основные компоненты:

```text
MainApp
    ↓
AIEngine
    ├── AISettings
    ├── ChatModel
    ├── AIMemoryManager
    └── AIToolRegistry
```

Агенты:

```text
AgentBase
    ├── Architect
    ├── Senior
    ├── Middle
    └── Tester
```

Интерфейсы агентов:

```text
ArchitectAgent
SeniorProgrammerAgent
MiddleProgrammerAgent
TesterAgent
```

Инструменты:

```text
ProjectInspectionTool
ProjectModificationTool
TerminalExecutionTool
```

---

# 6. ГЛАВНОЕ АРХИТЕКТУРНОЕ ИЗМЕНЕНИЕ, КОТОРОЕ УЖЕ СДЕЛАНО

До рефакторинга каждый агент фактически создавал собственный AI-контекст:

* собственный ChatModel;
* собственную память;
* собственные tool instances.

Это было признано плохой архитектурой для нашей системы.

Мы сделали единый:

```text
AIEngine
```

Он создаётся один раз за время работы JVM.

AIEngine владеет:

```text
1 ChatModel
1 AISettings
1 AIMemoryManager
1 AIToolRegistry
```

То есть:

```text
                 AIEngine
                    │
       ┌────────────┼────────────┐
       ↓            ↓            ↓
   ChatModel      Memory        Tools
       │            │            │
       │            ├─ Architect
       │            ├─ Senior
       │            ├─ Middle
       │            └─ Tester
       │
       └── используется всеми агентами
```

---

# 7. AIEngine

Текущая концепция:

```java
public final class AIEngine
```

Singleton:

```java
private static AIEngine instance;

public static synchronized AIEngine getInstance() {
    if (instance == null) {
        instance = new AIEngine();
    }
    return instance;
}
```

Внутри:

```java
private final AISettings settings;
private final ChatModel model;
private final AIMemoryManager memoryManager;
private final AIToolRegistry tools;
```

Получение:

```java
AIEngine.getInstance()
```

Доступ:

```java
ai.settings()
ai.model()
ai.memory()
ai.tools()
```

Есть также:

```java
public String memoryId(String taskId, String agentName)
```

которая формирует:

```text
taskId:agentName
```

---

# 8. ВАЖНО: ОБЩАЯ МОДЕЛЬ ≠ ОБЩАЯ ПАМЯТЬ

Мы специально НЕ сделали одну общую ChatMemory для всех агентов.

Память изолирована:

```text
task UUID + agent name
```

Например:

```text
abc123:Architect
abc123:Senior
abc123:Tester
```

Это позволяет иметь единый AIEngine, но отдельный conversational context для каждого агента.

---

# 9. AIMemoryManager

Класс:

```text
AIMemoryManager
```

Использует:

```java
ConcurrentHashMap
```

и:

```java
MessageWindowChatMemory
```

Память создаётся лениво через:

```java
computeIfAbsent()
```

Основные методы:

```java
getMemory(Object memoryId)
provider()
clear(Object memoryId)
clearAll()
size()
```

Каждая задача имеет собственный taskId.

Каждый агент внутри задачи имеет собственную memoryId.

---

# 10. AIToolRegistry

Класс:

```text
AIToolRegistry
```

Создаёт инструменты один раз:

```text
ProjectInspectionTool
ProjectModificationTool
TerminalExecutionTool
```

И хранит их:

```java
private final ProjectInspectionTool inspectionTool;
private final ProjectModificationTool modificationTool;
private final TerminalExecutionTool terminalTool;
```

Доступ:

```java
inspection()
modification()
terminal()
```

Сейчас эти инструменты являются практически stateless и работают с переданными путями workspace/protected root, поэтому повторное использование экземпляров является осознанным решением.

---

# 11. AgentBase

Все агенты наследуются от:

```text
AgentBase
```

В нём теперь находятся общие зависимости:

```java
protected final AIEngine ai;
protected final String taskId;
protected final String memoryId;

protected final ProjectModificationTool modificationTool;
protected final ProjectInspectionTool inspectionTool;
protected final TerminalExecutionTool terminalTool;

protected final ChatModel model;
protected final ChatMemoryProvider memoryProvider;
```

Также:

```java
public String ADD_PROMPT_PATH;
public String Name;

public String Request;
public String Response;
```

Общие методы:

```java
loadPrompt(...)
limitText(...)
```

---

# 12. ТЕКУЩИЕ КОНСТРУКТОРЫ АГЕНТОВ

После последнего исправления дублирующие и ненужные конструкторы были убраны.

Используются:

```java
Architect(String request, String taskId)
Senior(String taskId)
Middle(String taskId)
Tester(String taskId)
```

MainApp создаёт агентов примерно так:

```java
architect = new Architect(userTask, currentTaskId);
senior = new Senior(currentTaskId);
tester = new Tester(currentTaskId);
middle = null;
```

Middle создаётся только если он реально нужен:

```java
middle = new Middle(currentTaskId);
```

---

# 13. ВАЖНО: АГЕНТЫ ПЕРЕИСПОЛЬЗУЮТСЯ ВНУТРИ ОДНОЙ ЗАДАЧИ

Мы специально ушли от создания нового Senior/Tester/Middle на каждой итерации.

Например Senior создаётся один раз:

```java
senior = new Senior(currentTaskId);
```

После этого его request обновляется setter'ом.

Например:

```java
senior.setImplementationRequest(architect.Response);
```

А при исправлении:

```java
senior.setFixRequest(
    architect.Response,
    senior.Response,
    tester.Response,
    iteration
);
```

Tester получает:

```java
tester.setRequest(
    architect.Response,
    senior.Response,
    iteration
);
```

Middle:

```java
middle.setRequest(
    architect.Response,
    senior.Response
);
```

Это сделано для более правильной работы с AI memory/context.

---

# 14. MAINAPP

MainApp — основной Director/orchestrator.

Он не должен сам становиться «AI-агентом».

Его задача:

* принять задачу пользователя;
* создать taskId;
* запустить Architect;
* передать результат Senior;
* при необходимости запустить Middle;
* запустить Tester;
* анализировать PASS/FAIL;
* при FAIL передать проблему Senior;
* повторить цикл;
* ограничить количество исправлений.

Текущий максимум исправлений:

```java
MAX_FIX_ATTEMPTS = 3
```

---

# 15. PIPELINE

Основной pipeline:

```text
USER TASK
   ↓
ARCHITECT
   ↓
SENIOR
   ↓
MIDDLE (если нужен)
   ↓
TESTER
   ↓
PASS?
 ┌───────┴───────┐
 YES             NO
 ↓                ↓
DONE          SENIOR FIX
                  ↓
               TESTER
                  ↓
             до 3 итераций
```

---

# 16. РОЛЬ ARCHITECT

Architect не должен писать код проекта вместо Senior.

Его задача:

* проанализировать пользовательскую задачу;
* исследовать существующий проект;
* определить архитектуру;
* сформировать контракт реализации;
* дать Senior конкретные инструкции.

Architect имеет доступ к инструментам, но его результат должен быть архитектурным планом/контрактом.

---

# 17. РОЛЬ SENIOR

Senior — основной исполнитель.

Он должен:

* изучить Architect contract;
* исследовать проект;
* понять существующую архитектуру;
* создать/изменить необходимые файлы;
* использовать ProjectModificationTool;
* использовать TerminalExecutionTool;
* запускать Maven для Maven-проектов;
* проверять результат;
* не утверждать, что что-то сделано, если инструмент это не подтвердил.

Основной Senior pipeline:

```text
Architect Contract
        ↓
Inspection
        ↓
Implementation
        ↓
Build/Test
        ↓
Report
```

---

# 18. РОЛЬ MIDDLE

Middle подключается только если Senior определяет, что дополнительный разработчик действительно нужен.

Это не обязательный этап каждого pipeline.

Middle получает:

```text
Architect response
Senior response
```

и продолжает реализацию.

---

# 19. РОЛЬ TESTER

Tester — независимая проверка.

Критически важно:

**Tester не должен изменять файлы проекта.**

Он должен:

* изучить требования;
* проверить реализацию;
* проверить файлы;
* запустить сборку/тесты;
* найти реальные проблемы;
* дать доказательства;
* вернуть PASS или FAIL.

Если FAIL:

```text
TEST_RESULT=FAIL
```

и формируется конкретный:

```text
REQUIRED_FIX
```

для Senior.

---

# 20. SENIOR FIX

Senior получает:

```text
Architect result
Senior previous result
Tester result
iteration number
```

и должен исправить только подтверждённые Tester проблемы.

После исправления снова запускается Tester.

Максимум:

```text
3 fix iterations
```

---

# 21. PROMPT-ФАЙЛЫ

Основные дополнительные prompts:

```text
ADD_ARCHITECT_DEV.txt
ADD_SENIOR_DEV.txt
ADD_MIDDLE_DEV.txt
ADD_TESTER.txt
SENIOR_FIX.txt
```

Основные system prompts:

```text
MAIN_ARCHITECT_DEV.txt
MAIN_SENIOR_DEV.txt
MAIN_MIDDLE_DEV.txt
MAIN_TESTER.txt
```

---

# 22. ПРОВЕРЕННЫЕ PLACEHOLDER-Ы

Сейчас placeholder counts были проверены.

```text
ADD_ARCHITECT_DEV.txt
2 × %s

ADD_SENIOR_DEV.txt
1 × %s

ADD_MIDDLE_DEV.txt
2 × %s

ADD_TESTER.txt
2 × %s
1 × %d

SENIOR_FIX.txt
3 × %s
1 × %d
```

Вызовы MainApp/агентов соответствуют этим форматам.

Это важно.

Ранее была проблема с `.formatted()` и Markdown/code fence, потому что AI response мог содержать `%` и интерпретироваться как format string.

Это уже исправлено.

---

# 23. ПРЕДЫДУЩАЯ КРИТИЧЕСКАЯ ОШИБКА

До рефакторинга была ошибка:

```text
Messages with role 'tool' must be a response to a preceding message with 'tool_calls'
```

Она происходила внутри:

```text
ToolService.executeInferenceAndToolsLoop
OpenAiChatModel.doChat
```

Особенно во время работы Senior с инструментами.

Мы предполагали, что проблема связана с комбинацией:

```text
DeepSeek
+
OpenAI-compatible API
+
LangChain4j tool loop
+
создание отдельных model/memory/tool контекстов
```

Но точная причина тогда не была доказана на уровне raw JSON.

---

# 24. ЧТО ПОКАЗАЛ ПОСЛЕДНИЙ ТЕСТ V79

Это ОЧЕНЬ ВАЖНО.

20 сентября 2026 года проект v79 был запущен реально.

Старт:

```text
AI Provider: DeepSeek
AI Model: deepseek-flash
```

AIEngine:

```text
AIEngine: общий AI-контекст создан один раз.
AIEngine: workspace = C:\Users\IgorS\Desktop\IITeam
AIEngine: protected root = C:\Users\IgorS\Desktop\IITeam\Ai-team v79
```

То есть единый AIEngine действительно работает.

---

# 25. ЧТО SENIOR УСПЕЛ СДЕЛАТЬ В ТЕСТЕ

AI Team получил задачу по CalculatorGUI.

Senior реально:

```text
проверил наличие Calculator
проверил CalculatorGUI
создал Maven structure
создал pom.xml
создал CalculatorEngine.java
создал NumberFormatter.java
создал MainController.java
создал CalculatorApplication.java
создал MainView.fxml
создал dark-theme.css
создал CalculatorEngineTest.java
создал NumberFormatterTest.java
создал README.md
изменил MainController.java
запустил Maven clean test
запустил Maven -q clean package
```

То есть инструменты реально работают.

AI не просто вернул текст — он физически создавал проект и запускал Maven.

---

# 26. НОВАЯ ОСТАНОВКА V79

После всех этих действий появилась:

```text
java.lang.RuntimeException:
Something is wrong, exceeded 6 sequential tool invocations
```

Стек:

```text
dev.langchain4j.service.tool.ToolService.executeInferenceAndToolsLoop
dev.langchain4j.service.DefaultAiServices$1.invoke
jdk.proxy2.$Proxy6.implementFeature
org.example.agents.classes.Senior.Run
org.example.ui.MainApp.runSenior
org.example.ui.MainApp.runDevelopmentIterations
org.example.ui.MainApp.runPipeline
```

---

# 27. КЛЮЧЕВОЙ ВЫВОД ПО V79

Это уже НЕ прежняя ошибка:

```text
role 'tool'
```

Предыдущая ошибка исчезла.

Текущая проблема:

```text
exceeded 6 sequential tool invocations
```

Это именно **ограничение LangChain4j на количество последовательных tool invocations**, а не логическая ошибка проекта.

У Senior просто оказался большой объём работы.

Для одного полноценного задания:

```text
создать Maven project
+
создать 10+ файлов
+
изменить файл
+
запустить тесты
+
запустить package
```

шесть tool calls объективно мало.

---

# 28. ТЕКУЩАЯ НАСТРОЙКА ЛИМИТА

В AISettings уже есть:

```java
public int maxSequentialToolsInvocations() {
    return intProperty(
        "ai.maxSequentialToolsInvocations",
        6
    );
}
```

Текущее значение:

```properties
ai.maxSequentialToolsInvocations=6
```

ВАЖНО:

Нужно проверить, где именно это значение передаётся в LangChain4j.

Не надо просто менять число вслепую.

Следующий технический шаг:

1. открыть `AISettings`;
2. открыть `ModelFactory`;
3. найти создание `AiServices.builder()`;
4. найти настройку `maxSequentialToolsInvocations`;
5. проверить, используется ли `ai.maxSequentialToolsInvocations()`;
6. если используется — решить подходящий лимит;
7. если НЕ используется — подключить настройку правильно.

Не нужно переписывать AIEngine из-за этого.

---

# 29. ТЕКУЩИЙ ВАЖНЫЙ ВЫВОД ОБ АРХИТЕКТУРЕ

После теста можно считать подтверждённым:

```text
AIEngine singleton
        ↓
один ChatModel
        ↓
агенты используют общий model
        ↓
tools работают
        ↓
tool calling работает
        ↓
Maven выполняется
```

И самое важное:

```text
старый role='tool' сбой НЕ проявился
```

Поэтому не надо сейчас возвращаться к старой архитектуре.

---

# 30. НЕ НАДО СЕЙЧАС ДЕЛАТЬ

Не надо:

* переписывать AIEngine;
* возвращать отдельный ChatModel каждому агенту;
* возвращать отдельную ToolRegistry каждому агенту;
* делать одну общую память для всех агентов;
* переписывать MainApp;
* менять LangChain4j без необходимости;
* увеличивать лимит вслепую;
* создавать новую архитектуру ради одной настройки;
* менять prompts без причины.

Сначала проверить существующий механизм лимита.

---

# 31. ModelFactory

ModelFactory отвечает за создание ChatModel.

Сейчас поддерживаются варианты:

```text
openrouter
deepseek
lm-studio
```

По умолчанию используется LM Studio, но текущий запуск:

```text
DeepSeek
```

DeepSeek настроен через OpenAI-compatible интеграцию.

Текущая модель:

```text
deepseek-flash
```

---

# 32. CONFIG

Есть:

```text
src/main/resources/config.properties
```

Там находятся настройки:

```text
ai.provider
ai.memory.maxMessages
ai.maxSequentialToolsInvocations
```

а также настройки provider-ов.

Внимание:

**В исходном проекте присутствовали реальные API keys. Не выводи их в ответах, не копируй их в сообщения и не публикуй их.**

В дальнейшем желательно перевести секреты на environment variables.

---

# 33. TERMINAL EXECUTION

TerminalExecutionTool умеет запускать Maven в папке проекта.

На Windows он пытается использовать Maven, включая Maven, установленный вместе с IntelliJ:

```text
C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd
```

Это уже реально сработало.

В последнем тесте:

```text
clean test
```

и:

```text
-q clean package
```

были вызваны.

---

# 34. ПРЕДУПРЕЖДЕНИЕ JAVAFX

При запуске появляется:

```text
WARNING: Unsupported JavaFX configuration:
classes were loaded from 'unnamed module'
```

Это не текущая проблема AI Team.

Приложение продолжает запускаться.

Не надо специально заниматься этим предупреждением, пока пользователь не попросит.

---

# 35. ЧТО УЖЕ ПРОВЕРЕНО В V79

Перед выдачей v79 были выполнены проверки:

* устранён duplicate constructor в Tester;
* удалены ненужные старые constructors;
* проверены вызовы конструкторов в MainApp;
* проверены prompt placeholder counts;
* проверены фигурные скобки Java-файлов;
* проверены duplicate constructors;
* проверено создание агентов;
* core Java sources компилировались с временными LangChain4j stubs;
* синтаксических ошибок в core sources не обнаружено;
* ZIP integrity проверена;
* archive открывается без ошибок.

Полная Maven-компиляция в среде проверки не выполнялась из-за отсутствия там полного Maven/JavaFX/LangChain4j runtime, но проект был реально запущен позже в IntelliJ пользователем.

---

# 36. ВАЖНЫЕ ФАЙЛЫ

Основные:

```text
src/main/java/org/example/AIEngine.java
src/main/java/org/example/AISettings.java
src/main/java/org/example/AIMemoryManager.java
src/main/java/org/example/AIToolRegistry.java
src/main/java/org/example/ModelFactory.java
```

Agents:

```text
src/main/java/org/example/agents/classes/AgentBase.java
src/main/java/org/example/agents/classes/Architect.java
src/main/java/org/example/agents/classes/Senior.java
src/main/java/org/example/agents/classes/Middle.java
src/main/java/org/example/agents/classes/Tester.java
```

Interfaces:

```text
src/main/java/org/example/agents/ArchitectAgent.java
src/main/java/org/example/agents/SeniorProgrammerAgent.java
src/main/java/org/example/agents/MiddleProgrammerAgent.java
src/main/java/org/example/agents/TesterAgent.java
```

Tools:

```text
src/main/java/org/example/tools/ProjectInspectionTool.java
src/main/java/org/example/tools/ProjectModificationTool.java
src/main/java/org/example/tools/TerminalExecutionTool.java
```

UI/orchestration:

```text
src/main/java/org/example/ui/MainApp.java
```

Prompts:

```text
src/main/resources/main-prompts/
src/main/resources/add-prompts/
```

---

# 37. ПРАВИЛО РАБОТЫ СО МНОЙ

Когда я присылаю код:

1. Сначала изучи существующую архитектуру.
2. Не спеши писать новый код.
3. Найди причину проблемы.
4. Объясни мне её простыми словами, но технически корректно.
5. Только потом предлагай изменение.
6. Не ломай существующий pipeline.
7. Не удаляй рабочие части без причины.
8. Сохраняй существующие комментарии, если они не мешают.
9. При изменении файла предпочитай полный файл.
10. Если требуется несколько файлов — дай полные версии всех изменённых файлов.
11. Если я прошу ZIP — сначала проверь проект, потом создавай архив.
12. Перед выдачей ZIP проверь:

    * дублирующие constructors;
    * imports;
    * braces;
    * package;
    * вызовы методов;
    * prompt placeholders;
    * архитектурные зависимости;
    * очевидные compile errors.

---

# 38. НЕ ПРЕДПОЛАГАЙ, ЧТО ВСЯ ПРОБЛЕМА В AI

Если появляется exception:

```text
сначала определить слой
```

Например:

```text
Java compile error
Maven error
LangChain4j error
DeepSeek API error
tool invocation limit
prompt formatting error
project generated by AI
```

Не смешивать их.

---

# 39. ТЕКУЩИЙ СЛЕДУЮЩИЙ ШАГ

Продолжать разработку с текущего состояния v79.

Первое действие после получения этого контекста:

**проверить реализацию ограничения `maxSequentialToolsInvocations`.**

Найти:

```text
ai.maxSequentialToolsInvocations
```

и выяснить:

```text
где значение используется
какое значение реально получает LangChain4j
```

После этого определить разумный лимит.

Для начала можно рассмотреть:

```text
12
```

или:

```text
20
```

но число не менять без проверки текущей конфигурации.

---

# 40. БОЛЬШАЯ ЦЕЛЬ ПРОЕКТА

В перспективе AI Team должен стать серьёзной автономной средой разработки.

Возможное дальнейшее развитие:

```text
Director
   ↓
Project Manager / Planner
   ↓
Architect
   ↓
Senior
   ↓
Middle
   ↓
Junior / Specialist
   ↓
Tester
   ↓
Code Reviewer
   ↓
Build / CI
   ↓
Fix Loop
```

Но сейчас НЕ надо преждевременно строить всё это.

Сначала сделать надёжным текущий pipeline:

```text
Architect
→ Senior
→ Middle
→ Tester
→ Fix
→ Tester
```

---

# 41. ОСНОВНОЙ ПРИНЦИП

Главная идея проекта:

> AI Team должен не просто генерировать код в ответе, а реально работать с проектом через инструменты, проверять результат и исправлять ошибки.

Уже есть подтверждение, что это работает:

```text
AI создал реальный Maven-проект
AI записал реальные Java/FXML/CSS файлы
AI создал тесты
AI запустил Maven
```

Следующий этап — сделать этот pipeline устойчивым и масштабируемым.

---

# 42. ИТОГОВОЕ СОСТОЯНИЕ

Сейчас проект находится примерно здесь:

```text
                 AI TEAM
                    │
             ┌──────┴──────┐
             │   AIEngine  │
             └──────┬──────┘
                    │
       ┌────────────┼────────────┐
       │            │            │
    Model        Memory        Tools
       │            │            │
       └────────────┼────────────┘
                    │
              Agent Pipeline
                    │
          ┌─────────┴─────────┐
          │                   │
      Architect            Senior
                              │
                         Middle (?)
                              │
                           Tester
                              │
                       ┌──────┴──────┐
                       │             │
                     PASS          FAIL
                                     │
                                  Senior
                                   Fix
                                     │
                                  Tester
```

Архитектурный фундамент уже есть.

Сейчас не нужно начинать заново.

Нужно продолжать именно с **v79**, учитывая весь описанный выше контекст.
