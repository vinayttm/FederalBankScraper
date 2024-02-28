package com.example.federalBankScraper.Services;

import static com.example.federalBankScraper.Utils.AccessibilityUtil.*;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.federalBankScraper.MainActivity;
import com.example.federalBankScraper.Repository.QueryUPIStatus;
import com.example.federalBankScraper.Repository.SaveBankTransaction;
import com.example.federalBankScraper.Repository.UpdateDateForScrapper;
import com.example.federalBankScraper.Utils.AES;
import com.example.federalBankScraper.Utils.CaptureTicker;
import com.example.federalBankScraper.Utils.Config;
import com.example.federalBankScraper.Utils.SharedData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FederalRecorderService extends AccessibilityService {
    boolean loginOnce = true;
    int appNotOpenCounter = 0;
    final CaptureTicker ticker = new CaptureTicker(this::processTickerEvent);

    boolean isTransaction = false;
    boolean isLogin = false;

    @Override
    protected void onServiceConnected() {
        ticker.startChecking();
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }


    private void processTickerEvent() {
        Log.d("Ticker", "Processing Event");
        Log.d("Flags", printAllFlags());
        ticker.setNotIdle();

//        if (!SharedData.startedChecking) return;
        if (!MainActivity.isAccessibilityServiceEnabled(this, this.getClass())) {
            return;
        }

        AccessibilityNodeInfo rootNode = getTopMostParentNode(getRootInActiveWindow());
        if (rootNode != null) {
            if (findNodeByPackageName(rootNode, Config.packageName) == null) {
                if (appNotOpenCounter > 4) {
                    Log.d("App Status", "Not Found");
                    relaunchApp();
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    appNotOpenCounter = 0;
                    return;
                }
                appNotOpenCounter++;
            } else {
                Log.d("App Status", "Found");
                rootNode.refresh();
                checkForSessionExpiry();
                listAllTextsInActiveWindow(getTopMostParentNode(getRootInActiveWindow()));
                focusEditText();
                enterPin();
                viewFullStatement();
                backingProcess();
                readTransactions();
                rootNode.refresh();
            }
            rootNode.recycle();
        }
    }


    private void relaunchApp() {
        if (MainActivity.isAccessibilityServiceEnabled(this, this.getClass())) {
            new QueryUPIStatus(() -> {
                Intent intent = getPackageManager().getLaunchIntentForPackage(Config.packageName);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }, () -> {
                Toast.makeText(this, "Scrapper inactive", Toast.LENGTH_SHORT).show();
            }).evaluate();
        }
    }

    boolean isFocus = false;

    private void focusEditText() {
        if (isFocus) return;
        if (listAllTextsInActiveWindow(getTopMostParentNode(getRootInActiveWindow())).contains("Login Using MPIN")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean isClicked = performTap(371, 532);
            if (isClicked) {
                isFocus = true;
                isLogin = false;
                ticker.setNotIdle();
            }
        }

    }

    private void enterPin() {
        String loginPin = Config.loginPin;
        if (isLogin) return;
        if (isFocus) {
            if (listAllTextsInActiveWindow(getTopMostParentNode(getRootInActiveWindow())).contains("Login Using MPIN")) {
                for (char c : loginPin.toCharArray()) {
                    for (Map<String, Object> json : fixedPinedPosition()) {
                        String pinValue = (String) json.get("pin");
                        if (pinValue != null && json.get("x") != null && json.get("y") != null) {
                            if (pinValue.equals(String.valueOf(c))) {
                                int x = Integer.parseInt(json.get("x").toString());
                                int y = Integer.parseInt(json.get("y").toString());
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Clicked on X : " + x + " PIN " + pinValue);
                                System.out.println("Clicked on Y : " + y + " PIN " + pinValue);
                                performTap(x, y);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                isLogin = true;
                isFocus = false;
                ticker.setNotIdle();
            }
        }
    }


    private List<Map<String, Object>> fixedPinedPosition() {
        List<Map<String, Object>> jsonArray = new ArrayList<>();
        Map<String, Object> one = new HashMap<>();
        one.put("x", 123);
        one.put("y", 844);
        one.put("pin", "1");

        Map<String, Object> two = new HashMap<>();
        two.put("x", 356);
        two.put("y", 845);
        two.put("pin", "2");


        Map<String, Object> three = new HashMap<>();
        three.put("x", 600);
        three.put("y", 843);
        three.put("pin", "3");


        Map<String, Object> four = new HashMap<>();
        four.put("x", 121);
        four.put("y", 1020);
        four.put("pin", "4");

        Map<String, Object> five = new HashMap<>();
        five.put("x", 368);
        five.put("y", 1033);
        five.put("pin", "5");

        Map<String, Object> six = new HashMap<>();
        six.put("x", 600);
        six.put("y", 1231);
        six.put("pin", "6");

        Map<String, Object> seven = new HashMap<>();
        seven.put("x", 123);
        seven.put("y", 1211);
        seven.put("pin", "7");

        Map<String, Object> eight = new HashMap<>();
        eight.put("x", 361);
        eight.put("y", 1220);
        eight.put("pin", "8");

        Map<String, Object> nine = new HashMap<>();
        nine.put("x", 601);
        nine.put("y", 1212);
        nine.put("pin", "9");

        Map<String, Object> zero = new HashMap<>();
        zero.put("x", 363);
        zero.put("y", 1414);
        zero.put("pin", "0");

        jsonArray.add(one);
        jsonArray.add(two);
        jsonArray.add(three);
        jsonArray.add(four);
        jsonArray.add(five);
        jsonArray.add(six);
        jsonArray.add(seven);
        jsonArray.add(eight);
        jsonArray.add(nine);
        jsonArray.add(zero);
        return jsonArray;
    }

    private void viewFullStatement() {
        AccessibilityNodeInfo viewFullStatement = findNodeByText(getTopMostParentNode(getRootInActiveWindow()), "View Full Statement", true, false);
        if (viewFullStatement != null) {
            viewFullStatement.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            viewFullStatement.refresh();
            ticker.setNotIdle();
        }
    }


    private void backingProcess() {
        if (isTransaction) {
            if (listAllTextsInActiveWindow(getTopMostParentNode(getRootInActiveWindow())).contains("Account Statement")) {
                boolean isClicked = performTap(55, 104);
                if (isClicked) {
                    isTransaction = false;
                    ticker.setNotIdle();
                }
            }
        }
    }


    private String printAllFlags() {
        StringBuilder result = new StringBuilder();
        // Get the fields of the class
        Field[] fields = getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Object value = field.get(this);
                result.append(fieldName).append(": ").append(value).append("\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public boolean performTap(int x, int y) {
        Log.d("Accessibility", "Tapping " + x + " and " + y);
        Path p = new Path();
        p.moveTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(p, 0, 950));
        GestureDescription gestureDescription = gestureBuilder.build();
        boolean dispatchResult = false;
        dispatchResult = dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
        Log.d("Dispatch Result", String.valueOf(dispatchResult));
        return dispatchResult;
    }


    private String getUPIId(String description) {
        try {
            if (!description.contains("@")) return "";
            String[] split = description.split("/");
            String value = null;
            value = Arrays.stream(split).filter(x -> x.contains("@")).findFirst().orElse(null);
            return value != null ? value : "";
        } catch (Exception ex) {
            Log.d("Exception", ex.getMessage());
            return "";
        }
    }

    private String extractUTRFromDesc(String description) {
        try {
            String[] split = description.split("/");
            String value = null;
            value = Arrays.stream(split).filter(x -> x.length() == 12).findFirst().orElse(null);
            if (value != null) {
                return value + " " + description;
            }
            return description;
        } catch (Exception e) {
            return description;
        }
    }

    boolean scrollOnce = false;

    private void scrollOnce() {
        ticker.setNotIdle();
        AccessibilityNodeInfo scrollNode = findNodeByClassName(getTopMostParentNode(getRootInActiveWindow()), "androidx.recyclerview.widget.RecyclerView");
        if (scrollNode != null) {
//            if (scrollOnce) return;
            Rect scrollBounds = new Rect();
            scrollNode.getBoundsInScreen(scrollBounds);
            Log.d("ScrollBounds", "Top: " + scrollBounds.top + ", Bottom: " + scrollBounds.bottom);
            int startX = scrollBounds.centerX();
            int startY = scrollBounds.centerY();
            int endX = startX;
            int scrollDistance = 110;
            int endY = startY - scrollDistance;

            Log.d("SwipeGesture", "StartX: " + startX + ", StartY: " + startY + ", EndX: " + endX + ", EndY: " + endY);

            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, endY);
            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
            dispatchGesture(gestureBuilder.build(), null, null);
            scrollNode.recycle();


        }

    }


    public void readTransactions() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ticker.setNotIdle();
        JSONArray output = new JSONArray();
        int balanceIndex = 0;
        int lastUpdatedAt = 0;
        String totalBalance = "";

        List<String> itemsList = listAllTextsInActiveWindow(getTopMostParentNode(getRootInActiveWindow()));
        if (itemsList.contains("Search")) {
            for (int i = 0; i < itemsList.size(); i++) {
                if (itemsList.get(i).equals("Available Balance")) {
                    balanceIndex = i + 2;
                    System.out.println("Found Opening Balance at index: " + balanceIndex);
                    totalBalance = itemsList.get(balanceIndex);
                    break;
                }
            }
            if (!totalBalance.isEmpty()) {
                String cleanedBalance = totalBalance.replaceAll("[+₹]", "").replaceAll("[-₹]", "").replaceAll("\\s+", "");
                System.out.println("cleanedBalance " + cleanedBalance.trim());
                itemsList.removeIf(String::isEmpty);
                System.out.println("DataHere" + itemsList);
                for (int i = 0; i < itemsList.size(); i++) {
                    if (itemsList.get(i).equals("Last Updated at :")) {
                        lastUpdatedAt = i;
                        System.out.println("lastUpdatedAt Index : " + lastUpdatedAt);
                        break;
                    }
                }

                List<String> unfilterList = itemsList.subList(lastUpdatedAt, itemsList.size());
                unfilterList.remove("Last Updated at :");
                unfilterList.remove(0);
                unfilterList.remove("Search");
                unfilterList.remove("Filter");
                unfilterList.remove("Last Updated at :");
                unfilterList.remove(0);
                System.out.println("unfilterList " + unfilterList);
                String date = "";


                for (int i = 0; i < unfilterList.size(); ) {
                    JSONObject entry = new JSONObject();
                    String dateOrDescription = unfilterList.get(i);
                    if (isValidDate(dateOrDescription)) {
                        date = unfilterList.get(i);
                        String description = unfilterList.get(i + 1);
                        String balance = unfilterList.get(i + 2);
                        if (balance.contains("₹")) {
                            balance = balance.replace("₹", "").replaceAll("\\s+", "");
                        }
                        String time = unfilterList.get(i + 4);
                        System.out.println("Original Date =>" + date);
                        try {
                            entry.put("Amount", balance.toString());
                            entry.put("RefNumber", extractUTRFromDesc(description));
                            entry.put("Description", extractUTRFromDesc(description));
                            entry.put("AccountBalance", cleanedBalance.toString());
                            entry.put("CreatedDate", convertDateFormat(date + " " + time) + " " + time);
                            entry.put("BankName", Config.bankName + Config.bankLoginId);
                            entry.put("BankLoginId", Config.bankLoginId);
                            entry.put("UPIId", getUPIId(description));
                            output.put(entry);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        i = i + 5;

                    } else {
                        String description = unfilterList.get(i);
                        String balance = unfilterList.get(i + 1);
                        if (balance.contains("₹")) {
                            balance = balance.replace("₹", "").replaceAll("\\s+", "");
                        }
                        String time = unfilterList.get(i + 3);
                        try {
                            entry.put("Amount", balance.toString());
                            entry.put("RefNumber", extractUTRFromDesc(description));
                            entry.put("Description", extractUTRFromDesc(description));
                            entry.put("AccountBalance", cleanedBalance.toString());
                            entry.put("CreatedDate", convertDateFormat(date + " " + time) + " " + time);
                            entry.put("BankName", Config.bankName + Config.bankLoginId);
                            entry.put("BankLoginId", Config.bankLoginId);
                            entry.put("UPIId", getUPIId(description));
                            output.put(entry);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        i = i + 4;
                    }
                }

            }
            if (output.length() > 0) {
                Log.d("Final Json Output", output.toString());
                Log.d("API BODY", output.toString());
                Log.d("API BODY Length", String.valueOf(output.length()));
                JSONObject result = new JSONObject();
                try {
                    result.put("Result", AES.encrypt(output.toString()));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                new QueryUPIStatus(() -> {
                    new SaveBankTransaction(() -> {
                    }, () -> {

                    }).evaluate(result.toString());
                    new UpdateDateForScrapper().evaluate();
                    //    isTransaction = true;
                }, () -> {
                }).evaluate();
                isTransaction = true;
            }
        }
    }


    public static boolean isValidDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH);
        dateFormat.setLenient(false); // Disable leniency
        try {
            dateFormat.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String convertDateFormat(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm a", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void checkForSessionExpiry() {
        ticker.setNotIdle();
        AccessibilityNodeInfo targetNode1 = findNodeByText(getTopMostParentNode(getRootInActiveWindow()), "Session Expired", true, false);
        if (targetNode1 != null) {
            AccessibilityNodeInfo requestNode = findNodeByText(getTopMostParentNode(getRootInActiveWindow()), "Please Login again.", true, false);
            if (requestNode != null) {
                AccessibilityNodeInfo login = findNodeByText(getTopMostParentNode(getRootInActiveWindow()), "LOGIN", true, false);
                if (login != null) {
                    Rect outBounds = new Rect();
                    login.getBoundsInScreen(outBounds);
                    performTap(outBounds.centerX(), outBounds.centerY());
                    login.recycle();
                    requestNode.recycle();
                    targetNode1.refresh();
                    isFocus = false;
                    ticker.setNotIdle();
                }
            }
        }
    }
}
