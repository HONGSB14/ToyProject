package LDSS.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

@Service
public class UserService {
    /**
     * @param  paramValue API 매개변수
     * @param  URL    API URL
     * @todo API 로 값을 받아옴.
     * @return JSONArray
     */
    public JSONArray getAPI(String paramValue,String URL){
        String api_key="RGAPI-617ce822-c8f0-498a-8085-50ac6a754f33";
        JSONArray  ja= new JSONArray();
        try {
            StringBuilder urlBuilder = new StringBuilder(URL);
            urlBuilder.append(paramValue);
            urlBuilder.append("api_key=" + api_key);
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("Response code: " + conn.getResponseCode());
            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String result = rd.readLine();
            char replaceResult=result.charAt(0);
            String resultValue=String.valueOf(replaceResult);
            if(resultValue.equals("[")){
                JSONParser jsonParser = new JSONParser();
                JSONArray jsonArray=(JSONArray) jsonParser.parse(result);
                rd.close();
                conn.disconnect();
                return jsonArray;
            }else {
                JSONParser jsonParser = new JSONParser();
                JSONObject jo = (JSONObject) jsonParser.parse(result);
                ja.add(jo);
                rd.close();
                conn.disconnect();
                return ja;
            }
        } catch (Exception e) {
            System.err.println("불러오는 API에서 예기치 못한 오류가 발생했습니다.>>>" + e);
        }
        return null;
    }
    public  JSONArray userInfo(String myName) {
            JSONArray jsonArray =new JSONArray();
            String myId="";
            String puuId="";
            //입력값 공백 제거
            String reMyName=myName.replace(" ","")+"?";
            //해당 API URL
            String userInfoURL="https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/";
            //getAPI 메소드를 통해 API값 가져오기 (나)
            JSONArray myInfo =getAPI(reMyName,userInfoURL);
            JSONObject myInfoValue=(JSONObject) myInfo.get(0);
            JSONObject myStatus=(JSONObject) myInfoValue.get("status");

            //만약  status 값이 있다면  >> 오류발생  >> null 처리
            if( myStatus == null) {
                //만약  값이 존재 한다면 값 넘기기
                myId=(String)myInfoValue.get("id");
                puuId=(String)myInfoValue.get("puuid");
                jsonArray.add(myInfoValue);
                jsonArray.add(gameInfo(myId));
                jsonArray.add(mainChampion(myId));
                jsonArray.add(getMatchInfo(puuId));
            }else{
                return null;
            }
            return jsonArray;
    }

    public  JSONObject gameInfo(String myId){
        JSONObject jsonObject= new JSONObject();
        String replaceId=myId+"?";
        //해당 API URL
        String gameInfoURL="https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/";
        //API 에 값 넘기기
        jsonObject.put("myInfo",getAPI(replaceId,gameInfoURL));
        return jsonObject;
    }
    public JSONObject mainChampion(String myId){
        JSONObject jsonObject =new JSONObject();
        ArrayList<String> championName=new ArrayList<>();
        ArrayList<String> myMost3Champion=new ArrayList<>();
        JSONArray myChampList=new JSONArray();
        String champMapping="";
        String myChamp="";
        String myChampionTop=myId+"/top?count=5&";

        //해당 API URL
        String mainChampionURL="https://kr.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/";
        String championsURL="https://ddragon.leagueoflegends.com/cdn/10.6.1/data/ko_KR/champion.json";
        //챔피언 정보 API
        JSONArray zero =getAPI("?",championsURL);
        JSONObject data= (JSONObject) zero.get(0);
        JSONObject champions=(JSONObject) data.get("data");
        Iterator<String>c = champions.keySet().iterator();
        while (c.hasNext()){
                championName.add(c.next().toString());
        }
        //검색한 유저의  most 3 챔피언
        JSONArray myMost=getAPI(myChampionTop,mainChampionURL);
        for(int i =0; i<myMost.size(); i++){
               JSONObject myMost3=(JSONObject)myMost.get(i);
               myMost3Champion.add(myMost3.get("championId").toString());
        }
        
        //불러온 정보와 매칭 시켜서 이름 뽑아내기
        for(int i=0; i<championName.size(); i++){
            champMapping=championName.get(i);
            JSONObject champion=(JSONObject)champions.get(champMapping);
            String championKey=(String)champion.get("key");
            //모스트 챔피언 key 를 id 로 바꿔준다 . ex))  136-> Ahri
            for(int j=0; j<myMost3Champion.size(); j++){
                myChamp=myMost3Champion.get(j);
                if(championKey.equals(myChamp)){
                    myChampList.add(champion);
                }
            }
        }
        jsonObject.put("myMostTop",myChampList);
        return jsonObject;
    }

    public JSONArray getMatchInfo(String puuId){
        String MatchIdURL="https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"+puuId+"/ids?start=0&count=20&";
        JSONArray jsonArray=getAPI("",MatchIdURL);
        JSONArray ja= new JSONArray();
        String matchId="KR_6265554936?";
        if(jsonArray.size() !=0) {
//            for (int i = 0; i <jsonArray.size(); i++) {
//                matchId = (String) (jsonArray.get(i) + "?");
//                String matchInfoURL = "https://asia.api.riotgames.com/lol/match/v5/matches/";
//                ja.add(getAPI(matchId, matchInfoURL)) ;
//            }
            String matchInfoURL = "https://asia.api.riotgames.com/lol/match/v5/matches/";
            ja.add(getAPI(matchId, matchInfoURL)) ;
            return ja;
        }
        return null;
    }
}