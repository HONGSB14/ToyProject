package LDSS.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class UserService {
    /**
     *  @todo API 로 값을 받아온다.
     * @param  paramValue API 매개변수
     * @param  URL  API URL
     * @return JSONArray
     */
    public JSONArray getAPI(String paramValue,String URL){
        String api_key="RGAPI-c82cc8b0-b98b-4780-989c-746a7041a34d";
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

    /**
     *  @todo  화면에서 받아온 닉네임을 통하여 값을 리턴한다.
     * @param myName  화면에서 닉네임을 받아옴.
     * @return JSONArray myInfoValue,gameInfo,mainChampion,getMatchInfo
     */
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
                jsonArray.add(myInfoValue);                                                    //유저정보
                jsonArray.add(gameInfo(myId));                                              //랭크정보
                jsonArray.add(mainChampion(myId));                                   //모스트 챔피언 정보
                jsonArray.add(getMatchInfo(puuId));                                     //매치정보
//                jsonArray.add(dataProcessing(getMatchInfo(puuId)));       //매치정보
            }else{
                return null;
            }
            return jsonArray;
    }

    /**
     * @todo 랭크게임정보를 가져온다.
     * @param myId userInfo의 ID 값
     * @return JSONObject 랭크게임정보
     */
    public  JSONObject gameInfo(String myId){
        JSONObject jsonObject= new JSONObject();
        String replaceId=myId+"?";
        //해당 API URL
        String gameInfoURL="https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/";
        //API 에 값 넘기기
        jsonObject.put("myInfo",getAPI(replaceId,gameInfoURL));
        return jsonObject;
    }

    /**
     * @todo 검색한 소환사의 모스트 챔피언을 가져온다.
     * @param myId userInfo의 ID 값
     * @return JSONObject 모스트 챔피언 정보
     */
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
        //검색한 유저의  most 5 챔피언
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

    /**
     * @todo 매치정보 데이터불러온다.
     * @param puuId userInfo에서 가져오는 puuid
     * @return
     */
    public JSONArray getMatchInfo(String puuId){
        String MatchIdURL="https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"+puuId+"/ids?start=0&count=30&";
        JSONArray jsonArray=getAPI("",MatchIdURL);
        JSONArray  matchValue= new JSONArray();      //매치 데이터 받아온 값
        JSONArray ja= new JSONArray();                          //최종 리턴값 JSONArray
        ArrayList<String> matchIds= new ArrayList<>();  //타임라인
        String matchId="";                                                   //임시 매치 ID
        String queueId="";                                                   //큐 아이디 솔랭:420  팀랭 : 440
        int gameDuration=0;                                              //게임 지속 시간
        int gameCount=0;                                                    //솔로 랭크 게임 판수

        //matchId 유무 유효성 검사
        if(jsonArray.size() !=0) {
            System.out.println(jsonArray.size()+" GAMES Data Search Start .....");
            //matchId 만큼 반복
            for (int i = 0; i < jsonArray.size(); i++) {
                //솔로랭크게임 20판이라면 break (20판으로 데이터 가공)
                if(gameCount==20){
                    break;
                }
                matchId = (String) (jsonArray.get(i) + "?");
                String matchInfoURL = "https://asia.api.riotgames.com/lol/match/v5/matches/";
                matchValue.add(getAPI(matchId, matchInfoURL).get(0));
                JSONObject matchInfo=(JSONObject) matchValue.get(i);
                JSONObject info=(JSONObject) matchInfo.get("info");
                JSONArray participants=(JSONArray) info.get("participants");
                gameDuration=Integer.parseInt(String.valueOf(info.get("gameDuration")));
                queueId=String.valueOf(info.get("queueId"));
                if(queueId.equals("420") && gameDuration>300  ) {                                                                         //솔랭 데이터만 뽑기 , 다시하기 제외
                    gameCount++;                                                                                                                                       //랭크 게임 판수 20경기까지 조회하기위해 카운팅
                    JSONObject jo=new JSONObject();                                                                                                    //데이터 가공객체
                    for (int j=0; j<participants.size(); j++) {                                                                                            //각각의 매치경기 수 만큼
                        JSONObject member = (JSONObject) participants.get(j);                                                         //매치에 참여한 유저들 중에
                        if (member.get("puuid").equals(puuId)) {                                                                                  // 유저가 검색한 아이디 찾기

                            String lane = (String) member.get("teamPosition");                                                            //1. 검색한 아이디의 라인

                            jo.put("lane",lane);                                                                                                                      //데이터 가공값에 넣기
                            ja.add(jo);                                                                                                                                     //리턴값에 넣기
                        }
                    }
                }
           }
//            return ja;
           return matchValue;
        }
        return null;
    }

    /**
     * @param getMatchInfo
     * @return JSONObject 데이터 가공값
     * @todo 데이터를 가공하여 화면으로 넘긴다.
     */
    public JSONObject dataProcessing (JSONArray getMatchInfo){
        JSONObject jsonObject = new JSONObject();
        ArrayList<String> lane =new ArrayList<>();

        int i =0;
        String line ="";
        System.out.println(getMatchInfo);
        //공용  데이터에 담기
        for(i=0; i<getMatchInfo.size(); i++){
            JSONObject userGameInfo = (JSONObject) getMatchInfo.get(i);
            lane.add((String)userGameInfo.get("lane"));
        }
        line=userLane(lane);
        jsonObject.put("lane",line);                    //주 라인
        return jsonObject;
    }

    /**
     * @todo 검색한 유저의 주 라인을 구한다.
     * @param lane 라인 배열
     * @return String 주라인
     */
    public String userLane(ArrayList<String> lane){
        int mainLineGame=0;     //주 포지션 게임 판 수
        String mainLine="";        // 주 포지션

        int top=        Collections.frequency(lane,"TOP");
        int jungle=   Collections.frequency(lane,"JUNGLE");
        int mid=       Collections.frequency(lane,"MIDDLE");
        int bottom= Collections.frequency(lane,"BOTTOM");
        int support=Collections.frequency(lane,"UTILITY");

        //게임횟수가 가장 많은 라인 구하기
        if(top>=mainLineGame){
            mainLineGame=top;
            mainLine="Top";
        }
        if(jungle>=mainLineGame) {
            mainLineGame=jungle;
            mainLine="Jungle";
        }
        if(mid>=mainLineGame){
            mainLineGame=mid;
            mainLine="Mid";
        }
        if(bottom>=mainLineGame){
            mainLineGame=bottom;
            mainLine="Bot";
        }
        if(support>=mainLineGame){
            mainLineGame=support;
            mainLine="Support";
        }
        System.out.println("mainLine = " + mainLine);
        return mainLine;
    }

}

