# Reflection

## Solution & Key Design Decisions

My solution implements an automated crypto trading bot using Java and Spring Boot. The core trading logic is based on moving average crossovers, specifically using short-term (10-period) and long-term (50-period) Simple Moving Averages (SMA) as indicators. The bot analyzes historical price data to generate buy/sell signals: a buy is triggered when the short-term SMA crosses above the long-term SMA, and a sell when it crosses below. This approach was chosen for its simplicity, interpretability, and effectiveness in trending markets. The system is modular, with separate services for data fetching, trade execution, backtesting, and bot state management, allowing for easy testing and future extension.

### Architecture Choices

- MySQL for database because I am most familiar with it
- React for frontend because the app is SPA and since data should be constantly updated with React I only need to rerender the given component and not the full page resulting in performance and better UX
- CoinGecko is a practical and reliable data source as its API provides comprehensive, real-time, and historical market data with great documentation and easy setup.

## Trade-offs & Shortcuts

Due to time constraints, I focused on implementing the SMA crossover strategy and did not include more advanced indicators (e.g., RSI, MACD) or risk management features. Error handling is basic, and some edge cases (such as API failures or extreme market conditions) are handled with simple fallback logic. The user interface and reporting are minimal, prioritizing core functionality and test coverage. The bot work only with bitcoin trading on CoinGecko, but the design allows for easy extension to other assets and exchanges.

## External Tools & AI Usage

I used GitHub Copilot to assist with unit test generation and the frontend files due to focusing fully on the backend. I asked for example unit tests, code reviews and refactoring of big classes into components. All AI-generated code was reviewed, adapted, and tested to ensure correctness and integration with my project. I verified outputs by manual code inspection and functionality testing.

## Future Improvements

With more time, I would:

- Implement additional trading indicators and strategies.
- Add portfolio risk management and position sizing.
- Improve error handling and logging.
- Build a richer user interface and reporting dashboard.
- Optimize performance for real-time trading.
- Expand integration tests and backtesting scenarios.
- Support multiple cryptocurrencies and exchanges.
- Threshold-based notifications for trade signals.
- Estimation of trading fees.

# Project Setup

The project consists of a Spring Boot backend and a React frontend. To run the project, follow these steps:

1. **Backend Setup**:

   - Navigate to the backend directory:
     ```bash
     cd backend
     ```
   - Build the project using Gradle:
     ```bash
     ./gradlew build
     ```
   - Run the Spring Boot application:
     ```bash
     ./gradlew bootRun
     ```
   - The backend will start on `http://localhost:8080`.

2. **Frontend Setup**:
   - Navigate to the frontend directory:
     ```bash
     cd ../frontend
     ```
   - Install the dependencies:
     ```bash
     npm install
     ```
   - Start the React application:
     ```bash
     npm run start
     ```
   - The frontend will start on `http://localhost:3000`.

## Testing

To run the tests for the backend, navigate to the backend directory and execute:

- Navigate to the backend directory:
  ```bash
  cd backend
  ```
- Run the tests:
  ```bash
  ./gradlew test
  ```
