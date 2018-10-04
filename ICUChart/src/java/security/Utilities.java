/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

/**
 *
 * @author astell
 */
import java.util.Enumeration;
import java.util.Vector;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.servlet.http.HttpServletRequest;

public class Utilities {

    public Utilities() {
    }

    public boolean passwordCheck(String password1, String password2) {

        boolean passwordValid = true;

        //NEED TO DO A PASSWORD CHECK HERE
        if (!password1.equals(password2)) {
            //Check if the two password inputs match
            passwordValid = false;
        } else if (password1.length() < 8) {
            //Check length of password
            passwordValid = false;
        }

        //Check if there are any characters that are invalid
        if (passwordValid) {
            char[] acceptedCharsUpper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            char[] acceptedCharsLower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            char[] acceptedNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
            char[] acceptedPunctuation = {'!','#','$','%','&','(',')','*','+','-','.','/',':','<','=','>','?','@','[',']','^','_','{','|','}','~'};

            int inputLength = password1.length();
            String inputStr = password1;

            //For each character in the input string, check against the various arrays
            boolean upperFound = false;
            boolean lowerFound = false;
            boolean numberFound = false;
            boolean invalidChar = false;

            //for (int i = 0; i < inputLength; i++) {
            int i = 0;
            while (i < inputLength && !invalidChar) {

                boolean thisFound = false;

                char thisChar = inputStr.charAt(i);
                //System.out.println("" + thisChar);
                int charCount = 0;

                while (!thisFound && (charCount < acceptedCharsUpper.length)) {
                    if (thisChar == acceptedCharsUpper[charCount]) {
                        upperFound = true;
                        thisFound = true;
                    }

                    charCount++;
                }

                //Reset character count
                charCount = 0;
                while (!thisFound && (charCount < acceptedCharsLower.length)) {
                    if (thisChar == acceptedCharsLower[charCount]) {
                        lowerFound = true;
                        thisFound = true;
                    }

                    charCount++;
                }

                //Reset character count
                charCount = 0;
                while (!thisFound && (charCount < acceptedNumbers.length)) {
                    if (thisChar == acceptedNumbers[charCount]) {
                        numberFound = true;
                        thisFound = true;
                    }
                    charCount++;
                }
                
                //Reset character count (no demand for punctuation here)
                charCount = 0;
                while (!thisFound && (charCount < acceptedPunctuation.length)) {
                    if (thisChar == acceptedPunctuation[charCount]) {                        
                        thisFound = true;
                    }
                    charCount++;
                }

                if (!thisFound) {
                    invalidChar = true;
                }
                
                //System.out.println("invalidChar: " + invalidChar);

                i++;
                passwordValid = upperFound && lowerFound && numberFound && !invalidChar;
            }
        }
        return passwordValid;
    }

    public boolean emailCheck(String email) {
        boolean emailValid = true;
        if (email.length() > 100) {
            emailValid = false;
        }
        return emailValid;
    }

    public boolean institutionCheck(String institution) {
        boolean institutionValid = true;
        if (institution.length() > 200) {
            institutionValid = false;
        }
        return institutionValid;
    }

    public boolean nameCheck(String surname, String forename) {
        boolean nameValid = true;
        if (surname.length() > 200 || forename.length() > 200) {
            nameValid = false;
        }
        return nameValid;
    }

    public boolean checkInput(Enumeration inputs, HttpServletRequest request, String host, String username, String password) {

        boolean inputError = false;
        while (inputs.hasMoreElements()) {
            String input = (String) inputs.nextElement();

            //Check the request content types
            /*if (input.equals("dbn")) {
                String dbn = request.getParameter("dbn");
                if (!(dbn.equals("ACC")
                        || dbn.equals("APA")
                        || dbn.equals("Pheo")
                        || dbn.equals("CAH")
                        || dbn.equals("NAPACA"))) {
                    inputError = true;
                }
            } else */if (input.equals("study")) {
                String dbn = request.getParameter("study");
                if (!(dbn.equals("pmt")
                       && !dbn.equals("APA")
                       && !dbn.equals("Pheo")
                       && !dbn.equals("ACC"))) {
                    inputError = true;
                }
            } /*else if (input.equals("dbid")) {
                String dbid = request.getParameter("dbid");
                if (!(dbid.equals("1")
                        || dbid.equals("2")
                        || dbid.equals("3")
                        || dbid.equals("4")
                        || dbid.equals("5"))) {
                    inputError = true;
                }
            } */else if (input.equals("mod")) {
                String mod = request.getParameter("mod");
                if (!(mod.equals("1")
                        || mod.equals("2")
                        || mod.equals("3")
                        || mod.equals("4"))) {
                    inputError = true;
                }
            } else if (input.equals("modality")) {
                String modality = request.getParameter("modality");
                if (!(modality.equals("biomaterial")
                        || modality.equals("surgery")
                        || modality.equals("pathology")
                        || modality.equals("radiofrequency")
                        || modality.equals("chemotherapy")
                        || modality.equals("mitotane")
                        || modality.equals("chemoembolisation")
                        || modality.equals("followup")
                        || modality.equals("clinical")
                        || modality.equals("biochemical")
                        || modality.equals("imaging")
                        || modality.equals("interventions")
                        || modality.equals("tumordetails")
                        || modality.equals("complications")
                        || modality.equals("cardio")
                        || modality.equals("genetics")
                        || modality.equals("metabolomics")
                        || modality.equals("surgical_procedures")
                        || modality.equals("morphological_progression")
                        || modality.equals("biological_assessment")
                        || modality.equals("sm_study")
                        || modality.equals("samples")
                        || modality.equals("demographics")
                        || modality.equals("surveillance")
                        || modality.equals("sm_upload")
                        || modality.equals("radiotherapy"))) {
                    inputError = true;
                }
            } else if (input.equals("centerid")) {
                String modality = request.getParameter("centerid");
                
                Vector<String> centerCodes = this.getCenterCodes(host, username, password);
                int centerCodeNum = centerCodes.size();
                
                int codeCount = 0;
                boolean centerCodeFound = false;
                while(!centerCodeFound && codeCount < centerCodeNum){
                    String centerCodeIn = centerCodes.get(codeCount);
                    if(centerCodeIn.equals(modality)){
                        centerCodeFound = true;
                    }else{
                        codeCount++;
                    }                    
                }
                
                if(!centerCodeFound){
                    inputError = true;
                }
            } else if (input.equals("ensatidorder")) {
                String inputValue = request.getParameter("ensatidorder");
                if (!inputValue.equals("1") && !inputValue.equals("2") && !inputValue.equals("3") && !inputValue.equals("4") && !inputValue.equals("5") && !inputValue.equals("6") && !inputValue.equals("7")) {
                    inputError = true;
                }
            } else if (input.equals("search_filter")) {
                String inputValue = request.getParameter("search_filter");
                if (!inputValue.equalsIgnoreCase("all") && !inputValue.equalsIgnoreCase("local") && !inputValue.equalsIgnoreCase("national")) {
                    inputError = true;
                }
            }else if(input.equals("patient_search")){
                String inputValue = request.getParameter("patient_search");
                inputError = false;
            }
            
            /*
             * else if(input.equals("patient_search")){ String inputValue =
             * request.getParameter("ensatidorder"); if(!inputValue.equals("1")
             * && !inputValue.equals("2") && !inputValue.equals("3")){
             * inputError = true; }
    }
             */ else if (input.equals("incorrectlogin")) {
                String inputValue = request.getParameter("incorrectlogin");
                if (!(inputValue.equals("1"))) {
                    inputError = true;
                }
            } else if (input.equals("sessionexpired")) {
                String inputValue = request.getParameter("sessionexpired");
                if (!inputValue.equals("1")) {
                    inputError = true;
                }
            } else if (input.equals("loggedout")) {
                String inputValue = request.getParameter("loggedout");
                if (!inputValue.equals("1")) {
                    inputError = true;
                }
            } else if (input.equals("exportall")) {
                String inputValue = request.getParameter("exportall");
                if (!inputValue.equals("1")) {
                    inputError = true;
                }
            } else if (input.equals("backid")) {
                String inputValue = request.getParameter("backid");
                if (!(inputValue.equals("1")
                        || inputValue.equals("0"))) {
                    inputError = true;
                }
            } else if (input.equals("mainsearch")) {
                String inputValue = request.getParameter("mainsearch");
                if (!inputValue.equals("1")) {
                    inputError = true;
                }
            } else if (input.equals("showformsearch")) {
                String inputValue = request.getParameter("showformsearch");
                if (!inputValue.equals("1")) {
                    inputError = true;
                }
            } else if (input.equals("pid")) {
                String inputValue = request.getParameter("pid");
                //Check for integer number
                int inputValueInt = 0;
                try {
                    inputValueInt = Integer.parseInt(inputValue);
                } catch (NumberFormatException nfe) {
                    inputError = true;
                }

                if (inputValueInt < 1) {
                    inputError = true;
                }
            } else if (input.equals("modid")) {
                String inputValue = request.getParameter("modid");
                //Check for integer number
                int inputValueInt = 0;
                try {
                    inputValueInt = Integer.parseInt(inputValue);
                } catch (NumberFormatException nfe) {
                    inputError = true;
                }

                if (inputValueInt < 1) {
                    inputError = true;
                }
            } else if (input.equals("page")) {
                String inputValue = request.getParameter("page");
                //Check for integer number
                int inputValueInt = 0;
                try {
                    inputValueInt = Integer.parseInt(inputValue);
                } catch (NumberFormatException nfe) {
                    inputError = true;
                }

                if (inputValueInt < 1) {
                    inputError = true;
                }
            } else if (input.equals("createdid")) {
                String inputValue = request.getParameter("createdid");
                //Check for integer number
                int inputValueInt = 0;
                try {
                    inputValueInt = Integer.parseInt(inputValue);
                } catch (NumberFormatException nfe) {
                    inputError = true;
                }

                if (inputValueInt < 1) {
                    inputError = true;
                }
            } else {

                //Everything else includes all the database input, username and password
                //Check for SQL keywords, byte-code and script tags
                String inputStr = request.getParameter(input);

                //SQL injection check
                int inputLength = inputStr.length();

                //Check for single (or double) quotation marks, semi-colons and backslashes
                //char[] problemChars = {'\'', '\"', ';', '\\'};
                char[] problemChars = {'\"', ';', '\\'};

                for (int i = 0; i < inputLength; i++) {

                    char thisChar = inputStr.charAt(i);
                    boolean found = false;
                    int charCount = 0;
                    while (!found && (charCount < problemChars.length)) {

                        if (thisChar == problemChars[charCount]) {
                            found = true;
                        }

                        charCount++;
                    }

                    if (found) {
                        inputError = true;
                    }

                }

                //Check for SQL keywords
                String[] keywords = {"SELECT", "UPDATE", "INSERT", "ALTER", "DROP", "CREATE",
                "<script>","</script>",
                "<form>","</form>",
                "<object>","</object>",
                "<embed>","</embed>",
                "<applet>","</applet>"};
                
                for (int i = 0; i < keywords.length; i++) {

                    //Check for word size first
                    boolean wordMatch = true;
                    if (inputLength == keywords[i].length()) {
                        //Check the rest of the word for a match
                        int j = 0;
                        while (wordMatch && (j < keywords[i].length())) {
                            //Capture case differences here too (+32)
                            if (inputStr.charAt(j) == keywords[i].charAt(j) || inputStr.charAt(j) == (keywords[i].charAt(j) + 32)) {
                                j++;

                            } else {
                                wordMatch = false;

                            }
                            /*
                             * if (inputStr.charAt(j) !=
                             * sqlKeywords[i].charAt(j)) { wordMatch = false; }
                             * else { j++;
                    }
                             */

                        }
                    } else {
                        wordMatch = false;
                    }

                    if (wordMatch) {
                        inputError = true;
                    }

                }


                char[] acceptedCharsUpper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
                char[] acceptedCharsLower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
                char[] acceptedNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
                char[] acceptedSymbols = {'|', '&', ':', '/', '_', '.', '-', ' ', '!', ',', '\'', '%', '<', '>', '@', '+', '(', ')', '[', ']', '?', ';', '&','*','='};
                char[] acceptedDiacritics = {'á', 'Á', 'à', 'À', 'â', 'Â', 'å', 'Å', 'ã', 'Ã', 'ä', 'Ä', 'æ', 'Æ', 'ç', 'Ç', 'é', 'É', 'è', 'È',
                    'ê', 'Ê', 'ë', 'Ë', 'í', 'Í', 'ì', 'Ì', 'î', 'Î', 'ï', 'Ï', 'ñ', 'Ñ', 'ó', 'Ó', 'ò', 'Ò', 'ô', 'Ô', 'ø', 'Ø', 'õ', 'Õ', 'ö', 'Ö', 'ß', 'ú', 'Ú', 'ù', 'Ù',
                    'û', 'Û', 'ü', 'Ü', 'ÿ'};


                //For each character in the input string, check against the various arrays
                for (int i = 0; i < inputLength; i++) {

                    char thisChar = inputStr.charAt(i);
                    boolean found = false;
                    int charCount = 0;

                    while (!found && (charCount < acceptedCharsUpper.length)) {
                        if (thisChar == acceptedCharsUpper[charCount]) {
                            found = true;
                        }

                        charCount++;
                    }

                    //Reset character count
                    charCount = 0;
                    while (!found && (charCount < acceptedCharsLower.length)) {
                        if (thisChar == acceptedCharsLower[charCount]) {
                            found = true;
                        }

                        charCount++;
                    }

                    //Reset character count
                    charCount = 0;
                    while (!found && (charCount < acceptedNumbers.length)) {
                        if (thisChar == acceptedNumbers[charCount]) {
                            found = true;
                        }

                        charCount++;
                    }

                    //Reset character count
                    charCount = 0;
                    while (!found && (charCount < acceptedSymbols.length)) {
                        if (thisChar == acceptedSymbols[charCount]) {
                            found = true;
                        }

                        charCount++;
                    }

                    //Reset character count
                    charCount = 0;
                    while (!found && (charCount < acceptedDiacritics.length)) {
                        if (thisChar == acceptedDiacritics[charCount]) {
                            found = true;
                        }

                        charCount++;
                    }

                    if (!found) {
                        //Now finally throw the exception if a good character is not matched
                        inputError = true;
                    }

                }                
            }
        }
        return inputError;
    }
    
    private Vector<String> getCenterCodes(String host, String username, String password){
        
        Vector<String> centerCodes = new Vector<String>();
        
        try{
            Connection conn = this.connect("center_callout", host, username, password);
            String sql = "SELECT center_id FROM Center_Callout;";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()){
                String codeIn = rs.getString(1);
                if(codeIn == null){
                    codeIn = "";
                }
                if(!codeIn.equals("")){
                    centerCodes.add(codeIn);
                }
            }
            conn.close();
            
        }catch(Exception e){
            System.out.println("Error (getCenterCodes): " + e.getMessage());
        }
        
        return centerCodes;
    }
    
    private Connection connect(String dbName, String host, String username, String password) throws Exception {
        //String connectionURL = "jdbc:mysql://" + host + ":3306/" + dbName;        
        String connectionURL = "jdbc:mysql://localhost:3307/" + dbName; //CONNECTION BACK TO ENSAT DATA MACHINE
        
        username = "tunnel-2";
        password = "a2yX4sp";
                
        Class.forName("com.mysql.jdbc.Driver").newInstance();                
        Connection connection = DriverManager.getConnection(connectionURL, username, password);
        return connection;
    }
}
