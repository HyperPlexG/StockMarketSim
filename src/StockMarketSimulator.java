import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.time.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import java.util.Timer;
import java.util.TimerTask;

public class StockMarketSimulator extends JFrame {

    private Map<String, Stock> stocks = new HashMap<>();
    private Map<String, TimeSeriesCollection> stockData = new HashMap<>();
    private Map<String, TimeSeries> series = new HashMap<>();
    private double userBalance = 10000.0; // Starting balance
    private Map<String, Integer> userPortfolio = new HashMap<>();
    private JTextArea outputArea;
    private JPanel chartPanel;
    private Timer timer;
    private Random random = new Random();
    private DecimalFormat df = new DecimalFormat("#.##");
    private JLabel timerLabel;
    private int countdown = 5;

    // Stock class to hold stock information
    private class Stock {
        String symbol;
        String name;
        double price;
        double previousPrice;
        
        public Stock(String symbol, String name, double initialPrice) {
            this.symbol = symbol;
            this.name = name;
            this.price = initialPrice;
            this.previousPrice = initialPrice;
        }
    }

    public StockMarketSimulator() {
        // Initialize stocks
        initializeStocks();
        
        // Generate 5 minutes of historical data
        generateHistoricalData();
        
        // Setup UI
        setupUI();
        
        // Start price updates
        startPriceUpdates();
    }

    private void generateHistoricalData() {
        // Calculate how many 5-second intervals are in 10 minutes
        int intervalsIn10Minutes = (10 * 60) / 5;
        
        // Get current time
        Calendar cal = Calendar.getInstance();
        // Move back 5 minutes
        cal.add(Calendar.MINUTE, -10);
        
        // Generate historical data points
        for (int i = 0; i < intervalsIn10Minutes; i++) {
            for (Stock stock : stocks.values()) {
                // Random price change between -5% and +5%
                double changePercent = (random.nextDouble() * 10 - 5) / 100;
                stock.price *= (1 + changePercent);
                
                // Create a RegularTimePeriod for this historical point
                Second second = new Second(cal.getTime());
                series.get(stock.symbol).add(second, stock.price);
            }
            // Move time forward 5 seconds
            cal.add(Calendar.SECOND, 5);
        }
    }

    private void initializeStocks() {
        // American Stocks
stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 150.0));
stocks.put("GOOGL", new Stock("GOOGL", "Google Inc.", 2800.0));
stocks.put("MSFT", new Stock("MSFT", "Microsoft Corporation", 300.0));
stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 3300.0));
stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 900.0));
stocks.put("NVDA", new Stock("NVDA", "NVIDIA Corporation", 250.0));
// Indian Stocks
stocks.put("RELIANCE", new Stock("RELIANCE", "Reliance Ind Ltd.", 2550.0));
stocks.put("TCS", new Stock("TCS", "Tata Ltd.", 3550.0));
stocks.put("INFY", new Stock("INFY", "Infosys Ltd.", 1450.0));
stocks.put("HDFCBANK", new Stock("HDFCBANK", "HDFC Bank Ltd.", 155.0));
stocks.put("WIPRO", new Stock("WIPRO", "Wipro Ltd.", 410.0));
stocks.put("MARUTI", new Stock("MARUTI", "Maruti Ind Ltd.", 2550.0));

// UAE Stocks
stocks.put("EMAAR", new Stock("EMAAR", "Emaar Properties PJSC", 70.2));
stocks.put("ETISALAT", new Stock("ETISALAT", "Etisalat Group", 270.0));
stocks.put("DAMAC", new Stock("DAMAC", "Damac Properties Dubai", 100.8));
stocks.put("DEWA", new Stock("DEWA", "Dubai Electricity & Water", 20.5));
stocks.put("FAB", new Stock("FAB", "First Abu Dhabi Bank PJSC", 14.0));
stocks.put("ARAMEX", new Stock("ARAMEX", "Aramex PJSC", 30.4));


        // Initialize time series for each stock
        for (String symbol : stocks.keySet()) {
            series.put(symbol, new TimeSeries(symbol));
            stockData.put(symbol, new TimeSeriesCollection(series.get(symbol)));
        }
    }

    private void setupUI() {
        setTitle("Stock Market Simulator");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel actionPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        chartPanel = new JPanel(new BorderLayout());
        
        // Create timer label
        timerLabel = new JLabel("Next update in: 5s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Create components
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Create buttons
        JButton viewStocksButton = new JButton("View All Stocks");
        JButton buyStockButton = new JButton("Buy Stock");
        JButton sellStockButton = new JButton("Sell Stock");
        JButton viewPortfolioButton = new JButton("View Portfolio");
        JButton viewGraphButton = new JButton("View Graph");

        // Add action listeners
        viewStocksButton.addActionListener(e -> viewStocks());
        buyStockButton.addActionListener(e -> buyStock());
        sellStockButton.addActionListener(e -> sellStock());
        viewPortfolioButton.addActionListener(e -> viewPortfolio());
        viewGraphButton.addActionListener(e -> viewGraph());

        // Add components to panels
        actionPanel.add(viewStocksButton);
        actionPanel.add(buyStockButton);
        actionPanel.add(sellStockButton);
        actionPanel.add(viewPortfolioButton);
        actionPanel.add(viewGraphButton);
        actionPanel.add(timerLabel);

        controlPanel.add(actionPanel, BorderLayout.NORTH);
        controlPanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to frame
        add(controlPanel, BorderLayout.WEST);
        add(chartPanel, BorderLayout.CENTER);

        // Initial balance display
        updateOutput("Welcome to Stock Market Simulator!\nYour initial balance: $" + df.format(userBalance));
    }

    private void startPriceUpdates() {
        timer = new Timer();
        
        // Timer for updating the countdown display
        Timer countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    countdown--;
                    if (countdown < 0) {
                        countdown = 5;
                    }
                    timerLabel.setText("Next update in: " + countdown + "s");
                });
            }
        }, 0,1000); // Update every second
        
        // Timer for updating prices
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updatePrices();
                countdown = 5;
            }
        }, 0, 10000); // Update every 10 seconds
    }

    private void updatePrices() {
    for (Stock stock : stocks.values()) {
            stock.previousPrice = stock.price;
        // Random price change between -5% and +5%
        double changePercent = (random.nextDouble() * 10 - 5) / 100;
        stock.price *= (1 + changePercent);

        // Update time series data
        series.get(stock.symbol).add(new Second(), stock.price);
    }

    // Refresh output with updated stock prices
    StringBuilder sb = new StringBuilder("Current Stock Prices:\n\n");
    for (Stock stock : stocks.values()) {
            String arrow = stock.price >= stock.previousPrice ? "↑" : "↓";
            String color = stock.price >= stock.previousPrice ? "\u001B[32m" : "\u001B[31m"; 
        sb.append(String.format("%s (%s): $%s\n",
            stock.name, stock.symbol, df.format(stock.price)));
    }
    SwingUtilities.invokeLater(() -> updateOutput(sb.toString()));

    // If graph is currently shown, update it
    if (chartPanel.getComponents().length > 0) {
        String currentSymbol = chartPanel.getName();
        if (currentSymbol != null) {
            updateGraph(currentSymbol);
        }
    }
}


    private void viewStocks() {
        StringBuilder sb = new StringBuilder("Current Stock Prices:\n\n");
        for (Stock stock : stocks.values()) {
            String arrow = stock.price >= stock.previousPrice ? "↑" : "↓";
            sb.append(String.format("%s (%s): $%s\n", 
                stock.name, stock.symbol, df.format(stock.price), arrow));
        }
        updateOutput(sb.toString());
    }
    
    private void updateGraph(String symbol) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            stocks.get(symbol).name + " Stock Price",
            "Time",
            "Price ($)",
            stockData.get(symbol),
            true,
            true,
            false
        );

        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Set up custom renderer for color changes
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        TimeSeries currentSeries = series.get(symbol);
        
        // Set line color based on price movement
        int itemCount = currentSeries.getItemCount();
        if (itemCount >= 2) {
            TimeSeriesDataItem current = currentSeries.getDataItem(itemCount - 1);
            TimeSeriesDataItem previous = currentSeries.getDataItem(itemCount - 2);
            
            Color lineColor = current.getValue().doubleValue() >= previous.getValue().doubleValue() 
                ? Color.GREEN : Color.RED;
            renderer.setSeriesPaint(0, lineColor);
        }

        plot.setRenderer(renderer);

        // Update the chart panel
        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart));
        chartPanel.setName(symbol);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void updateOutput(String message) {
        outputArea.setText(message);
        // Make arrows visible in different colors
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    private void buyStock() {
        String symbol = JOptionPane.showInputDialog("Enter stock symbol to buy:").toUpperCase();
        if (!stocks.containsKey(symbol)) {
            JOptionPane.showMessageDialog(this, "Invalid stock symbol!");
            return;
        }

        String quantityStr = JOptionPane.showInputDialog("Enter quantity to buy:");
        try {
            int quantity = Integer.parseInt(quantityStr);
            double totalCost = stocks.get(symbol).price * quantity;

            if (totalCost > userBalance) {
                JOptionPane.showMessageDialog(this, "Insufficient funds!");
                return;
            }

            userBalance -= totalCost;
            userPortfolio.put(symbol, userPortfolio.getOrDefault(symbol, 0) + quantity);
            
            updateOutput(String.format("Bought %d shares of %s at $%s per share\nNew balance: $%s",
                quantity, symbol, df.format(stocks.get(symbol).price), df.format(userBalance)));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!");
        }
    }

    private void sellStock() {
        String symbol = JOptionPane.showInputDialog("Enter stock symbol to sell:").toUpperCase();
        if (!userPortfolio.containsKey(symbol)) {
            JOptionPane.showMessageDialog(this, "You don't own any shares of this stock!");
            return;
        }

        String quantityStr = JOptionPane.showInputDialog("Enter quantity to sell:");
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity > userPortfolio.get(symbol)) {
                JOptionPane.showMessageDialog(this, "You don't own enough shares!");
                return;
            }

            double totalValue = stocks.get(symbol).price * quantity;
            userBalance += totalValue;
            
            int remainingShares = userPortfolio.get(symbol) - quantity;
            if (remainingShares == 0) {
                userPortfolio.remove(symbol);
            } else {
                userPortfolio.put(symbol, remainingShares);
            }

            updateOutput(String.format("Sold %d shares of %s at $%s per share\nNew balance: $%s",
                quantity, symbol, df.format(stocks.get(symbol).price), df.format(userBalance)));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!");
        }
    }

    private void viewPortfolio() {
        StringBuilder sb = new StringBuilder("Your Portfolio:\n\n");
        sb.append("Cash Balance: $").append(df.format(userBalance)).append("\n\n");
        
        double totalPortfolioValue = userBalance;
        
        for (Map.Entry<String, Integer> entry : userPortfolio.entrySet()) {
            String symbol = entry.getKey();
            int quantity = entry.getValue();
            double currentPrice = stocks.get(symbol).price;
            double totalValue = currentPrice * quantity;
            totalPortfolioValue += totalValue;
            
            sb.append(String.format("%s: %d shares @ $%s = $%s\n",
                symbol, quantity, df.format(currentPrice), df.format(totalValue)));
        }
        
        sb.append("\nTotal Portfolio Value: $").append(df.format(totalPortfolioValue));
        updateOutput(sb.toString());
    }

    private void viewGraph() {
        String symbol = JOptionPane.showInputDialog("Enter stock symbol to view:").toUpperCase();
        if (!stocks.containsKey(symbol)) {
            JOptionPane.showMessageDialog(this, "Invalid stock symbol!");
            return;
        }
        updateGraph(symbol);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new StockMarketSimulator().setVisible(true);
        });
    }
}
