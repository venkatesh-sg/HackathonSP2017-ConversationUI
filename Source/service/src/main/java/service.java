import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Venkatesh on 4/14/2017.
 */
@WebServlet(name = "service",urlPatterns = "/service")
public class service extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //Parsing the request
        StringBuilder buffer = new StringBuilder();
        BufferedReader bufferreader = req.getReader();
        String line;
        while ((line = bufferreader.readLine()) != null) {
            buffer.append(line);
        }
        String data = buffer.toString();
        String output = "";
        JSONObject params = new JSONObject(data);

        //Get result object from request
        JSONObject result = params.getJSONObject("result");
        //Get user Query from result object
        String reolvedQuery=result.getString("resolvedQuery");
        //get parameters object from result object
        JSONObject parameters = result.getJSONObject("parameters");

        //Checking for problem in parameter key's
        if(parameters.has("problem")){
            //Get the value of problem
            String value=parameters.get("problem").toString();

            System.out.println("Problem from user is "+value);

            //Get relevant data of value to send it to NLP
            BufferedReader textreader = new BufferedReader(new FileReader("Data/"+value+".txt"));
            String query= "";
            String txtline;
            while((txtline = textreader.readLine()) != null)
            {
                query=query+txtline;
            }
            textreader.close();
            JSONObject request_obj=  new JSONObject();
            request_obj.put("TestData",query);

            //NLP and RandomForest hosted Url
            String url = "https://specialization.herokuapp.com/specializationgen";
            URL obj = new URL(url);
            //Opening new HTTP connection to get specialization
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(request_obj.toString());
            wr.flush();
            wr.close();
            System.out.println("Classification request sent to Classification Service hosted in Heroku");

            //Response from NLP service
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String specialization = response.toString().replace("[","").replace("]","").replace("'","");
            //finished getting specialization

            //Saving specialization class for future use
            try{
                PrintWriter writer = new PrintWriter("Data/specialization.txt", "UTF-8");
                writer.println(specialization);
                writer.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }


            //Response to user
            JSONObject js2 = new JSONObject();
            js2.put("speech", "Would you like to make appointment with nearest doctor or with good rating");
            js2.put("displayText", "Would you like to make appointment with nearest doctor or with good rating");
            js2.put("source", "RandomForest");
            output = js2.toString();

        }
        //check for filters in parameter Key's
        else if(parameters.has("filters")){
            //Get the filter
            String filter=parameters.getString("filters").toString();

            //Get specialization
            BufferedReader textreader = new BufferedReader(new FileReader("Data/specialization.txt"));
            String specialization= "";
            String txtline;
            while((txtline = textreader.readLine()) != null)
            {
                specialization=specialization+txtline;
            }
            textreader.close();


            //Query to mlab to get data hosted in MongoDB
            String query="https://api.mlab.com/api/1/databases/doctors/collections/doc?q={%22specialization%22:%22" + specialization + "%22}&s={%22"+filter+"%22: -1}&l=1&apiKey=hpFqsjMO3PXiLz3ybR983TFTbfy8K9Mx";

            //New HTTP request to database
            URL obj = new URL(query);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //Parsing reponse
            JSONArray jsonArray = new JSONArray(response.toString());
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            String name=jsonObject.getString("name");

            //Save doctor name for future
            try{
                PrintWriter writer = new PrintWriter("Data/name.txt", "UTF-8");
                writer.println(name);
                writer.close();
            } catch (IOException e) {
            }


            //Response to user
            JSONObject js = new JSONObject();
            js.put("speech", "Making appointment with "+name+"");
            js.put("displayText", "");
            js.put("source", "Doctor database");
            output=js.toString();

        }
        //Check for time in parameters Key's
        else if(parameters.has("time")){
            String time=parameters.getString("time");

            //Get doctor name
            BufferedReader textreader = new BufferedReader(new FileReader("Data/name.txt"));
            String name= "";
            String txtline;
            while((txtline = textreader.readLine()) != null)
            {
                name=name+txtline;
            }
            textreader.close();

            //Response to user
            JSONObject js = new JSONObject();
            js.put("speech", "Making appointment with "+name+" at "+time);
            js.put("displayText", "Making appointment with "+name+" at"+time);
            js.put("source", "UI");
            output=js.toString();
            //we can add callender notification to user for the  appointment
        }

        resp.setHeader("Content-type", "application/json");
        resp.getWriter().write(output);

    }

}
