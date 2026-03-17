# Obsidian

Obsidian brings modern development conventions to the Spark Java framework. Built on top of Spark, it adds annotation-based routing, fluent database migrations, middleware system, CSRF protection, authentication, and more — all while keeping the simplicity of a micro-framework.

## Why this project?

Spark Java is an excellent micro-framework, but it lacks modern conventions. This boilerplate fills the gap by adding:

- **Routing annotations** — no more manually declared routes
- **Middleware system** — `@Before` and `@After` annotations with built-in middlewares
- **Fluent migrations** — Laravel-inspired database migrations
- **Security & Auth** — UserDetailsService abstraction with role-based access control
- **CSRF protection** — annotation-based CSRF validation
- **Repository pattern** — automatic dependency injection
- **Template engine** — Pebble integrated directly into controllers
- **Flash messages** — temporary notifications without external dependencies

## Main Features

| Feature | Description |
|---------|-------------|
| **Annotation-based routing** | `@GET`, `@POST`, `@PUT`, `@DELETE`, `@PATCH` on your methods |
| **Middleware system** | `@Before` / `@After` with built-in CORS, rate limiting, logging, API keys |
| **Fluent migrations** | `table.string("title").notNull()` instead of raw SQL |
| **Security & Auth** | `UserDetailsService` + `@HasRole` for role-based protection |
| **CSRF Protection** | `@CsrfProtect` annotation with automatic token validation |
| **Dependency Injection** | Automatically injects your `@Repository` into controllers |
| **ActiveRecord models** | ActiveJDBC with getters/setters to manipulate your models cleanly |
| **Integrated templating** | `render("view.html", data)` directly in your controllers |
| **Flash messages** | `redirectWithFlash()` for temporary notifications |
| **Custom Error Handler** | Detailed stack traces in dev, clean pages in production |
| **LiveComponents** | Reactive server-side components without writing JavaScript |
| **Validation** | Fluent, Laravel-style request validation |
| **CLI** | Built-in command-line interface with annotations |
| **Cache** | Unified cache facade with in-memory and Redis drivers |
| **Storage** | Unified file storage with local and custom disk support |
| **WebSockets** | Bi-directional real-time communication |
| **Server-Sent Events** | Uni-directional server-to-client streaming |

## Quick Start

```bash
git clone https://github.com/obsidian-framework/obsidian-skeleton
cd obsidian-skeleton
mvn compile exec:java
```

→ The app runs on `http://localhost:8888`

## Tech Stack

- **Spark Java** - Web micro-framework
- **ActiveJDBC** - Lightweight ORM with ActiveRecord pattern
- **Pebble** - Modern template engine
- **Maven** - Build & dependency management

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL / PostgreSQL / SQLite

---

## Documentation

### Application Startup

Obsidian uses a convention-over-configuration approach with automatic class discovery. One line of code starts everything.

```java
public class Main {
 public static void main(String[] args) throws Exception {
 Obsidian.run(Main.class);
 }
}
```

The framework automatically discovers and registers classes based on their annotations: `@Controller`, `@Repository`, `@UserDetailsServiceImpl`, `@GlobalAdvice`.



---

### Routing

```java
@GET(value = "/articles", name = "articles.index")
@POST(value = "/articles", name = "articles.store")
@PUT(value = "/articles/:id", name = "articles.update")
@DELETE(value = "/articles/:id", name = "articles.delete")
@PATCH(value = "/articles/:id", name = "articles.patch")
```

Named routes can be referenced in Pebble templates:

```twig
<a href="{{ route(name='articles.show') }}">View Article</a>
```

---

### Middleware

```java
@Before({CorsMiddleware.class, ApiKeyMiddleware.class, RateLimitMiddleware.class})
@GET(value = "/api/data", name = "api.data")
private Object data(Request req, Response res) { ... }
```

**Built-in middlewares:** `LoggingMiddleware`, `CorsMiddleware`, `RateLimitMiddleware` (100 req/min per IP), `TimingMiddleware`, `ApiKeyMiddleware`.

Custom middleware:

```java
public class MaintenanceMiddleware implements Middleware {
 @Override
 public void handle(Request req, Response res) throws Exception {
 if (System.getenv("MAINTENANCE_MODE") != null) {
 res.redirect("/maintenance");
 throw new Exception("Maintenance mode");
 }
 }
}
```

---

### Controllers

```java
@Controller
public class ArticleController extends BaseController {

 @GET(value = "/articles", name = "articles.index")
 private Object index(Request req, Response res) {
 return render("articles/index.html", Map.of("title", "My articles"));
 }

 @POST(value = "/articles", name = "articles.store")
 private Object store(Request req, Response res) {
 String title = req.queryParams("title");
 Article article = new Article();
 article.set("title", title);
 article.saveIt();
 res.redirect("/articles");
 return null;
 }
}
```

Repositories are injected automatically:

```java
@GET(value = "/articles", name = "articles.index")
private Object index(ArticleRepository articleRepo) {
 List<Article> articles = articleRepo.findAll().stream().toList();
 return render("articles/index.html", Map.of("articles", articles));
}
```

---

### Models (ActiveJDBC)

```java
public class Article extends Model {
 public String getTitle() { return getString("title"); }
 public void setTitle(String t) { set("title", t); }
}
```

```java
// Create
Article article = new Article();
article.setTitle("My title");
article.saveIt();

// Read
Article article = Article.findById(1);
LazyList<Article> published = Article.where("status = ?", 1);

// Update & Delete
article.setTitle("New title");
article.saveIt();
article.delete();
```

---

### Migrations

```java
public class CreateArticlesTable extends Migration {

 @Override
 public void up() {
 createTable("articles", table -> {
 table.id();
 table.string("title").notNull();
 table.text("content").notNull();
 table.integer("status").defaultValue(1);
 table.timestamps();
 });
 }

 @Override
 public void down() {
 dropTable("articles");
 }
}
```

Available types: `id()`, `string()`, `text()`, `integer()`, `bigInteger()`, `decimal()`, `boolean()`, `date()`, `datetime()`, `timestamp()`, `timestamps()`.

Modifiers: `.notNull()`, `.defaultValue()`, `.unique()`, `.foreignKey()`.

---

### Repositories

```java
@Repository
public class ArticleRepository {

 public LazyList<Article> findAll() { return Article.findAll(); }

 public LazyList<Article> findPublished() {
 return Article.where("status = ?", 1);
 }

 public void create(String title, String content) {
 Article article = new Article();
 article.setTitle(title);
 article.setContent(content);
 article.saveIt();
 }
}
```

---

### Database Configuration

Configure via `.env` file:

```bash
# SQLite
DB_TYPE=sqlite
DB_PATH=database.db

# MySQL
DB_TYPE=mysql
DB_HOST=localhost
DB_PORT=3306
DB_NAME=my_database
DB_USER=root
DB_PASSWORD=secret

# PostgreSQL
DB_TYPE=postgresql
DB_HOST=localhost
DB_PORT=5432
DB_NAME=my_database
DB_USER=postgres
DB_PASSWORD=secret
```

Since **Obsidian 1.3.0**, the database connection is managed automatically per request — no manual `DB.withConnection()` wrapping needed in repositories.

Use `DB.withTransaction()` for atomic operations:

```java
DB.withTransaction(() -> {
 sender.set("balance", sender.getBigDecimal("balance").subtract(amount));
 receiver.set("balance", receiver.getBigDecimal("balance").add(amount));
 sender.saveIt();
 receiver.saveIt();
 return true;
});
```

---

### Seeder

```java
@Seeder(priority = 10)
public class PostSeeder implements SeederInterface
{
 @Inject
 private PostRepository postRepository;

 @Override
 public void seed()
 {
 DB.withConnection(() -> {
 if (postRepository.findAll().isEmpty()) {
 postRepository.create(
 "fore-magna-nostris-et-propinqua",
 "Fore magna nostris et propinqua.",
 "Ultima Syriarum est Palaestina per intervalla magna protenta.",
 "admin"
 );
 }
 return null;
 });
 }
}
```

---

### Security & Authentication

```java
@UserDetailsServiceImpl
public class AppUserDetailsService implements UserDetailsService {

 @Override
 public UserDetails loadByUsername(String username) {
 return userRepository.findByUsername(username);
 }

 @Override
 public UserDetails loadById(Object id) {
 return userRepository.findById(id);
 }
}
```

```java
// Login
Auth.login(username, password, req); // includes brute-force protection
Auth.logout(req.session());

// Checks
Auth.isLogged(req);
Auth.hasRole(req, "ADMIN");
Auth.user(req); // cached per request

// Passwords
Auth.hashPassword("secret");
Auth.checkPassword("secret", hash);
```

Protect routes with annotations:

```java
@GET("/dashboard")
@RequireLogin
public String dashboard(Request req, Response res) { ... }

@Controller
@HasRole("ADMIN")
public class AdminController extends BaseController { ... }
```

Inject the current user directly:

```java
@GET("/profile")
@RequireLogin
public String profile(Request req, Response res, @CurrentUser User user) {
 return render("profile.html", Map.of("user", user));
}
```

**Brute-force protection:** after 5 failed attempts, the IP and username are locked for 15 minutes. A `LoginLockedException` is thrown.

**Bearer token (API):**

```java
@TokenResolverImpl
public class AppTokenResolver implements TokenResolver {
 @Override
 public UserDetails resolve(String token) {
 ApiToken t = ApiToken.findFirst("token = ?", token);
 if (t == null || t.isExpired()) return null;
 return User.findById(t.getLong("user_id"));
 }
}

@GET("/api/me")
@Bearer
public String me(Request req, Response res, @CurrentUser UserDetails user) { ... }
```

---

### CSRF Protection

```java
@CsrfProtect
@POST(value = "/articles", name = "articles.store")
private Object store(Request req, Response res) { ... }
```

In templates:

```twig
<form method="POST" action="/articles">
 {{ csrf_field() | raw }}
 ...
</form>
```

For AJAX: send the token via the `X-CSRF-TOKEN` header.

---

### Flash Messages

```java
return redirectWithFlash(req, res, "success", "Welcome back!", "/dashboard");
return redirectWithFlash(req, res, "error", "Invalid credentials", "/login");
```

In your layout template:

```twig
{{ flash() }}
```

Configure globally:

```java
@Config
public class NotifConfig implements ConfigInterface {
 @Override
 public void configure() {
 FlashConfig.setDuration(5000);
 FlashConfig.setPosition("top-right"); // top-right, top-left, bottom-right, bottom-left, top-center, bottom-center
 }
}
```

---

### WebSockets

```java
@org.eclipse.jetty.websocket.api.annotations.WebSocket
@WebSocket("/ws/chat")
public class ChatHandler {

 private static final Map<Session, String> users = new ConcurrentHashMap<>();

 @OnWebSocketConnect
 public void onConnect(Session session) throws IOException {
 session.getRemote().sendString("Welcome!");
 }

 @OnWebSocketMessage
 public void onMessage(Session session, String message) throws IOException {
 for (Session s : users.keySet()) {
 if (s.isOpen()) s.getRemote().sendString(message);
 }
 }

 @OnWebSocketClose
 public void onClose(Session session, int code, String reason) {
 users.remove(session);
 }
}
```

Use the `/ws/` prefix to avoid conflicts with HTTP routes.

---

### Server-Sent Events (SSE)

```java
@SSE(value = "/notifications/stream", name = "notifications.stream")
public Object stream(Request req, Response res) {
 res.raw().setCharacterEncoding("UTF-8");
 PrintWriter writer = res.raw().getWriter();

 try {
 writer.write("data: {\"message\":\"Hello\"}\n\n");
 writer.flush();
 } finally {
 writer.close();
 }
 return null;
}
```

Client-side:

```javascript
const eventSource = new EventSource('/notifications/stream');
eventSource.onmessage = (event) => console.log(JSON.parse(event.data));
```

**WebSocket vs SSE at a glance:**

| | WebSocket | SSE |
|---|---|---|
| Direction | Bi-directional | Server → Client only |
| Reconnection | Manual | Automatic |
| Best for | Chat, games | Notifications, dashboards |

---

### LiveComponents

Reactive, real-time interfaces without writing JavaScript.

```java
@LiveComponentImpl
public class Counter extends LiveComponent {

 @State
 private int count = 0;

 public void increment() { count++; }
 public void decrement() { count--; }
 public int getCount() { return count; }

 @Override
 public String template() { return "components/counter.html"; }
}
```

```html
<div live:id="{{ _id }}">
 <h2>Count: {{ count }}</h2>
 <button live:click="increment">+ Increment</button>
 <button live:click="decrement">- Decrement</button>
</div>
```

In your layout:

```twig
{{ livecomponents_scripts | raw }}
```

Available directives: `live:click`, `live:model`, `live:blur`, `live:lazy`, `live:debounce`, `live:poll`, `live:init`, `live:confirm`, `live:loading`.

---

### Validation

```java
ValidationResult result = RequestValidator.validateSafe(req, Map.of(
 "username", "required|min:3|max:20|alphanumeric",
 "email", "required|email|unique:users,email",
 "password", "required|min:8|confirmed"
));

if (result.fails()) {
 return render("register.html", Map.of("errors", result.getErrors()));
}
```

Available rules: `required`, `email`, `min:N`, `max:N`, `between:N,M`, `numeric`, `integer`, `alpha`, `alphanumeric`, `url`, `confirmed`, `unique:table,column`, `in:a,b,c`, `regex:pattern`.

---

### CLI

```java
@Command(name = "greet", description = "Greet a user", aliases = {"g"})
public class GreetCommand implements Runnable {

 @Param(index = 0, name = "name", description = "Name to greet")
 private String name;

 @Option(name = "--upper", description = "Print in uppercase", flag = true)
 private boolean upper;

 @Override
 public void run() {
 String message = "Hello, " + name + "!";
 Printer.ok(upper ? message.toUpperCase() : message);
 }
}
```

```bash
obsidian greet Alice --upper
obsidian --help
obsidian --version
```

Output helpers: `Printer.ok()`, `Printer.error()`, `Printer.warning()`, `Printer.info()`, `Printer.note()`, `Printer.caution()`.

Interactive components: `CliComponents.Prompt.text()`, `.confirm()`, `.select()`, `ProgressBar`, `Spinner`, `Table`.

---

### Cache

```bash
CACHE_DRIVER=memory # default
CACHE_DRIVER=redis
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=secret
```

```java
Cache.put("user:42", user, 60); // store for 60s
User user = Cache.get("user:42");
Cache.has("user:42");
Cache.forget("user:42");

// Cache-aside pattern
List<Faction> factions = Cache.remember("factions:all", 300,
 () -> factionRepository.findAll()
);
```

---

### Storage

```bash
STORAGE_LOCAL_ROOT=storage/app
STORAGE_LOCAL_URL=https://example.com/storage
STORAGE_DISK=local
```

```java
@Inject
private StorageManager storage;

storage.put("reports/q3.pdf", bytes);
byte[] content = storage.get("reports/q3.pdf");
storage.exists("reports/q3.pdf");
storage.delete("reports/q3.pdf");
List<String> files = storage.list("reports");
String url = storage.url("reports/q3.pdf");

// File uploads
Part part = request.raw().getPart("avatar");
MultipartFile file = new MultipartFile(part);
storage.putFile("avatars", file); // UUID filename generated automatically

// Switch disk
storage.disk("public").put("images/banner.png", bytes);
```

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## License

[MIT](LICENSE)