# Checkers Engine - Spring Boot Project

A complete checkers game implementation with AI opponent, built in Java 17 with Spring Boot 3.3.x.

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Git**

### 1. Clone and Setup

```bash
git clone <your-repo-url>
cd checkers-engine
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Test the API

```bash
# Create a new game
curl -X POST 'http://localhost:8080/api/games?aiDepth=4'

# View the board (ASCII format)
curl 'http://localhost:8080/api/games/{GAME_ID}/board?format=ascii'

# Make a move
curl -X POST 'http://localhost:8080/api/games/{GAME_ID}/move?aiColor=BLACK' \
  -H 'Content-Type: application/json' \
  -d '{ "path": [{"row":5,"col":1},{"row":4,"col":0}] }'
```

## 🎮 How to Play

### Game Rules

- **RED goes first** (American checkers rules)
- **Pieces move diagonally forward** (men) or in all directions (kings)
- **Captures are mandatory** - if you can capture, you must
- **Multi-jumps** are supported and required
- **King promotion** happens automatically when reaching the back rank
- **Game ends** when a player has no legal moves

### API Endpoints

| Method | Endpoint                             | Description                                 |
| ------ | ------------------------------------ | ------------------------------------------- |
| `POST` | `/api/games?aiDepth=4`               | Create new game with AI difficulty 1-5      |
| `GET`  | `/api/games/{id}/board?format=json`  | Get board state as JSON                     |
| `GET`  | `/api/games/{id}/board?format=ascii` | Get board as readable text                  |
| `POST` | `/api/games/{id}/move?aiColor=BLACK` | Make a move, optionally trigger AI response |

### Example Game Flow

1. **Create Game**

   ```bash
   curl -X POST 'http://localhost:8080/api/games?aiDepth=3'
   ```

   Response: `{"gameId":"abc123","turn":"RED","ascii":"..."}`

2. **View Board**

   ```bash
   curl 'http://localhost:8080/api/games/abc123/board?format=ascii'
   ```

3. **Make Move**

   ```bash
   curl -X POST 'http://localhost:8080/api/games/abc123/move?aiColor=BLACK' \
     -H 'Content-Type: application/json' \
     -d '{ "path": [{"row":5,"col":1},{"row":4,"col":0}] }'
   ```

4. **AI Responds** (if it's AI's turn)

## 🧪 Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=RulesEngineTests
```

### Run with Coverage

```bash
mvn test jacoco:report
```

### Test Categories

- **RulesEngineTests**: Game rules, move validation, captures
- **AIPlayerTests**: AI algorithm, move selection, evaluation
- **GameTests**: Game state management, turn handling
- **ModelTests**: Data structures, piece behavior, board operations

## 🏗️ Project Structure

```
src/
├── main/java/com/example/checkers/
│   ├── CheckersApplication.java          # Spring Boot entry point
│   ├── api/                             # REST API controllers
│   │   ├── GameController.java          # Game endpoints
│   │   ├── MoveRequest.java             # Move request DTO
│   │   └── BoardResponse.java           # Board response DTO
│   ├── core/                            # Game logic
│   │   ├── Game.java                    # Game state management
│   │   └── GameService.java             # Game storage service
│   ├── engine/                          # Game engine
│   │   ├── RulesEngine.java             # Move validation & generation
│   │   └── AIPlayer.java                # AI opponent (minimax)
│   ├── model/                           # Data models
│   │   ├── Board.java                   # 8x8 game board
│   │   ├── Piece.java                   # Game pieces (RED/BLACK, men/kings)
│   │   ├── Move.java                    # Move representation
│   │   ├── Square.java                  # Board coordinates
│   │   ├── Color.java                   # Player colors
│   │   └── GameResult.java              # Game outcome
│   └── util/                            # Utilities
└── test/java/com/example/checkers/      # Test classes
```

## 🔧 Development

### Building

```bash
mvn clean compile
```

### Running in Development Mode

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Ddebug=true"
```

### Package for Production

```bash
mvn clean package
java -jar target/checkers-engine-0.0.1-SNAPSHOT.jar
```

### IDE Setup

- **IntelliJ IDEA**: Import as Maven project
- **Eclipse**: Import existing Maven project
- **VS Code**: Install Java extensions, open as Java project

## 🎯 Key Features

### Game Engine

- ✅ **Complete checkers rules** implementation
- ✅ **Forced capture** enforcement
- ✅ **Multi-jump** support
- ✅ **King promotion** on back rank
- ✅ **Game end detection**

### AI Opponent

- ✅ **Minimax algorithm** with configurable depth
- ✅ **Material evaluation** (men=1, kings=2.5)
- ✅ **Mobility bonus** for strategic play
- ✅ **Consistent move selection**

### API Design

- ✅ **RESTful endpoints** for game management
- ✅ **JSON request/response** formats
- ✅ **ASCII board visualization** for debugging
- ✅ **Move validation** and error handling

## 🐛 Troubleshooting

### Common Issues

**Port already in use:**

```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

**Java version issues:**

```bash
# Check Java version
java -version

# Should show Java 17 or higher
```

**Maven not found:**

```bash
# Install Maven (macOS)
brew install maven

# Install Maven (Ubuntu/Debian)
sudo apt install maven
```

**Tests failing:**

```bash
# Clean and rebuild
mvn clean test

# Run with debug output
mvn test -X
```

### Debug Mode

Enable debug logging in `application.properties`:

```properties
logging.level.com.example.checkers=DEBUG
logging.level.org.springframework.web=DEBUG
```

## 📚 Learning Resources

### Checkers Rules

- [American Checkers Rules](https://en.wikipedia.org/wiki/Checkers)
- [Forced Capture Rule](https://www.checkers.com/rules)

### AI Algorithms

- [Minimax Algorithm](https://en.wikipedia.org/wiki/Minimax)
- [Game Tree Search](https://en.wikipedia.org/wiki/Game_tree)

### Spring Boot

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Web MVC](https://docs.spring.io/spring-framework/docs/current/reference/web.html)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Run tests: `mvn test`
5. Commit: `git commit -am 'Add feature'`
6. Push: `git push origin feature-name`
7. Submit a pull request

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 🆘 Support

- **Issues**: Create a GitHub issue
- **Questions**: Check existing issues or create a new one
- **Contributions**: Pull requests welcome!

---

**Happy coding! 🎮✨**
