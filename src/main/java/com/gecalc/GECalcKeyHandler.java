package com.gecalc;

import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.client.input.KeyListener;

import java.awt.event.KeyEvent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Slf4j
class GECalcKeyHandler implements KeyListener
{
    @Inject
    private Client client;

    public static boolean containsOperators(String inputString)
    {
        // Quick check to see if entered value contains an operator
        if (inputString.contains("+"))
            return true;
        if (inputString.contains("-"))
            return true;
        if (inputString.contains("*"))
            return true;
        if (inputString.contains("/"))
            return true;

        return false;
    }

    private boolean isQuantityInput()
    {
        /*
        Figure out of user has entered a quantity into the GE quantity or price input.
        7 = Quantity input (ge, trade, bank)
         */
        return client.getVar(VarClientInt.INPUT_TYPE) == 7;
    }

    private int runExpression(String expression) throws ScriptException
    {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        String result = String.valueOf(engine.eval(expression).toString());

        // Try the conversion to an integer, catch any errors and default to 1gp
        try {
            // Check if result contains a decimal, if so get the integer value
            // Integer.parseInt() didn't seem to do the job
            if (result.contains("."))
                return Integer.parseInt(result.split("\\.")[0]);
            else
                return Integer.parseInt(result);
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            return 1;
        } catch (NumberFormatException e){
            e.printStackTrace();
            return 1;
        }
    }

    private int convertKMBValue(String sanitisedInput)
    {
        // Check that the entered value is in the correct format 0 || 0.0 with trailing k, m or b
        if (sanitisedInput.matches("[0-9]+\\.[0-9]+[kmb]") || sanitisedInput.matches("[0-9]+[kmb]")) {
            // Get which unit the user ended the value with, k, m or b
            char foundUnit = sanitisedInput.charAt(sanitisedInput.length() - 1);
            // Get the numerical value of the entered value, no k, m or b
            double amountEntered = Double.parseDouble(sanitisedInput.substring(0, sanitisedInput.length() - 1));
            // Multiply the entered value by the unit
            double newAmount;
            switch (foundUnit) {
                case 'k':
                    newAmount = amountEntered * 1000;
                    break;
                case 'm':
                    newAmount = amountEntered * 1000000;
                    break;
                case 'b':
                    newAmount = amountEntered * 1000000000;
                    break;
                default:
                    newAmount = 0;
                    break;
            }

            return (int) newAmount;
        }

        // If the format of the entered value doesn't contain a unit, remove all dots
         return Integer.parseInt(sanitisedInput.replaceAll("\\.",""));
    }

    private void parseQuantity()
    {
        int calculatedValue = 0;
        final String rawInput = client.getVar(VarClientStr.INPUT_TEXT);
        // Remove spaces and force lowercase
        String sanitisedInput = rawInput.toLowerCase().replaceAll("\\s+","");

        // Check if the entered value contains operators
        if (containsOperators(sanitisedInput)) {
            try {
                // Run the entered expression and attempt to get the value
                calculatedValue = runExpression(sanitisedInput);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        else {
            try{
                // Try and parsed the entered unit k, m or b
                calculatedValue = convertKMBValue(sanitisedInput);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // Set the value to the parsed value
        client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(calculatedValue));
    }

    private void appendStringToValue(String toAppend, Boolean checkForMultiple)
    {
        // Take current input text and append a period (decimal)
        final String currentValue = client.getVar(VarClientStr.INPUT_TEXT);
        if(currentValue.equals("")) {
            return;
        }
        if(checkForMultiple && currentValue.contains(".")) {
            return;
        }
        String newValue = currentValue + toAppend;
        client.setVar(VarClientStr.INPUT_TEXT, newValue);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if(e.getKeyCode() == KeyEvent.VK_ENTER && isQuantityInput())
        {
            // Intercept quantity for parsing
            parseQuantity();
        }
        else if((e.getKeyCode() == KeyEvent.VK_PERIOD || e.getKeyCode() == KeyEvent.VK_DECIMAL) && isQuantityInput())
        {
            // Override input for decimal point
            appendStringToValue(".", true);
        }
        else if((e.getKeyCode() == 32) && isQuantityInput())
        {
            // Override input for space - for nice formatting
            appendStringToValue(" ", false);
        }
        else if((e.getKeyCode() == 107 || e.getKeyCode() == 109 || e.getKeyCode() == 106 || e.getKeyCode() == 111) && isQuantityInput())
        {
            // Override input for operators + - * /
            appendStringToValue(String.valueOf(e.getKeyChar()), false);
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }
}