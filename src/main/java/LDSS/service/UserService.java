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
        String api_key="RGAPI-6c764a57-2a60-49b6-892a-d9db0636f92f";
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
     *  @todo  화면에서 받아온 닉네임을 통해  함수를 만들어 값을 리턴한다.
     * @param myName  화면에서 닉네임을 받아옴.
     * @return JSONArray myInfoValue,gameInfo,mainChampion,getMatchInfo
     */
    public  JSONArray userInfo(String myName) {
            JSONArray jsonArray =new JSONArray();
            try{
                String myId="";
                String puuId="";
                //입력값 공백 제거
                String reMyName=myName.replace(" ","%20")+"?";          // %20 은 공백을 설정한 값을 뜻한다.
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
//                    jsonArray.add(getMatchInfo(puuId));                                     //매치정보
                    jsonArray.add(dataProcessing(getMatchInfo(puuId)));       //매치정보
                }else{
                    return null;
                }
                return jsonArray;
            }catch (Exception e){
                System.err.println("userInfo Error = " + e);
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
        String championsURL="https://ddragon.leagueoflegends.com/cdn/13.1.1/data/ko_KR/champion.json";
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
        //최종 리턴값 JSONArray
        JSONArray ja= new JSONArray();
       //매치 정보가 없을 경우에 대한 null 및 예외처리
        try{
            String MatchIdURL="https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"+puuId+"/ids?start=0&count=30&";
            JSONArray jsonArray=getAPI("",MatchIdURL);
            JSONArray  matchValue= new JSONArray();      //매치 데이터 받아온 값
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
                    if(queueId.equals("420") && gameDuration>300  ) {                                                                                                             //솔랭 데이터만 뽑기 , 다시하기 제외
                        gameCount++;                                                                                                                                                                            //랭크 게임 판수 20경기까지 조회하기위해 카운팅
                        JSONObject jo=new JSONObject();                                                                                                                                         //데이터 가공객체
                        for (int j=0; j<participants.size(); j++) {                                                                                                                                 //각각의 매치경기 수 만큼
                            JSONObject member = (JSONObject) participants.get(j);                                                                                              //매치에 참여한 유저들 중에
                            if (member.get("puuid").equals(puuId)) {                                                                                                                        // 유저가 검색한 아이디 찾기
                                JSONObject challenges = (JSONObject) member.get("challenges");
                                String lane = (String) member.get("teamPosition");                                                                                                                                      //1. 검색한 아이디의 라인
                                String championName=(String)member.get("championName");                                                                                                               //2. 검색한 아이디의 챔피언
                                int totalDamage=Integer.parseInt(String.valueOf(member.get("totalDamageDealtToChampions")));                                                    //3. 검색한 아이디의 데미지 총량
                                int wardPlaced=Integer.parseInt(String.valueOf(member.get("wardsPlaced")));                                                                                      //4. 검색한 아이디의 와드 설치 개수
                                int wardsKilled=Integer.parseInt(String.valueOf(member.get("wardsKilled")));                                                                                      //5. 검색한 아이디의 와드 삭제 개수
                                int visionScore=Integer.parseInt(String.valueOf(member.get("visionScore")));                                                                                         //6. 검색한 아이디의 시야점수
                                int  laneMinionsFirst10Minutes=Math.round(Float.parseFloat(String.valueOf(challenges.get("laneMinionsFirst10Minutes"))));     //7. 10분동안 미니언 처치 수
                                int jungleCsBefore10Minutes=Math.round(Float.parseFloat(String.valueOf(challenges.get("jungleCsBefore10Minutes"))));            //8. 10분동안 정글 처치 수
                                int totalMinionsKilled = Integer.parseInt(String.valueOf(member.get("totalMinionsKilled")));                                                              //9. 총 미니언 처치 수
                                int neutralMinionsKilled = Integer.parseInt(String.valueOf(member.get("neutralMinionsKilled")));                                                   //10. 총 정글몹 처치 수
                                int  damagePerMinute =(int)(Math.round(Float.parseFloat(String.valueOf(challenges.get("damagePerMinute")))));                         //11. 분당 데미지
                                double kda=Math.round(Float.parseFloat(String.valueOf(challenges.get("kda")))*100)/100.0;                                                               //12. kda
                                //데이터 가공값에 넣기
                                jo.put("lane",lane);                                                                                                                     //1.라인
                                jo.put("championName",championName);                                                                            //2.챔피언 이름
                                jo.put("totalDamages",totalDamage);                                                                                       //3. 토탈데미지
                                jo.put("wardsPlaced",wardPlaced);                                                                                          //4. 와드 설치 개수
                                jo.put("wardsKilled",wardsKilled);                                                                                          //5. 와드 삭제 개수
                                jo.put("visionScore",visionScore);                                                                                            //6. 시야 점수
                                jo.put("laneMinionsFirst10Minutes",laneMinionsFirst10Minutes);                                    //7. 10분동안 미니언 처치 수
                                jo.put("jungleCsBefore10Minutes",jungleCsBefore10Minutes);                                          //8. 10분동안 정글 처치 수
                                jo.put("totalMinionsKilled", totalMinionsKilled);                                                                  //9. 게임 토탈 미니언 처치 수
                                jo.put("neutralMinionsKilled",neutralMinionsKilled);                                                        //10.게임 토탈 정글 미니언 처치 수
                                jo.put("damagePerMinute",damagePerMinute);                                                                  //11. 분당 데미지
                                jo.put("kda",kda);                                                                                                                      //12. kda
                                ja.add(jo);                                                                                                                                   //리턴값에 넣기
                            }
                        }
                    }
               }
              return ja;
    //            return matchValue;
            }
        }catch (Exception e){
            System.err.println("getMatchInfo Error = " + e);
        }
        return ja;
    }
    /**
     * @todo 데이터를 가공하여 화면으로 넘긴다.
     * @param getMatchInfo
     * @return JSONObject 데이터 가공값
     */
    public JSONObject dataProcessing (JSONArray getMatchInfo){
        JSONObject jsonObject = new JSONObject();
        ArrayList<String> lane =new ArrayList<>();
        ArrayList<String> ChampName=new ArrayList<>();
        HashMap<String,ArrayList<Integer>> damageInfo= new HashMap<>();
        HashMap<String,ArrayList<String>> getGameChampName= new HashMap<>();
        HashMap<String,ArrayList<Integer>> wardInfo =new HashMap<>();
        HashMap<String,ArrayList<Integer>> minionKilledInfo = new HashMap<>();
        int i =0;
        String line ="";
        String champRole="";
        //공용  데이터에 담기
        for(i=0; i<getMatchInfo.size(); i++){
            JSONObject userGameInfo = (JSONObject) getMatchInfo.get(i);
            lane.add((String)userGameInfo.get("lane"));
            ChampName.add((String)userGameInfo.get("championName"));
        }

        line=userLane(lane);
        champRole=getRole(ChampName);
        damageInfo=getTotalDamage(getMatchInfo,line);
        wardInfo=getWardInfo(getMatchInfo,line);
        minionKilledInfo=getMinionsKilledInfo(getMatchInfo,line);
        getGameChampName=getGameChampName(getMatchInfo,line);
        jsonObject.put("lane", line);                                                                       //주 라인
        jsonObject.put("champRole", champRole);                                              //주요역할군
        jsonObject.put("damageInfo",damageInfo);                                             //토탈 대미지 ( 챔피언에게 가한 데미지)
        jsonObject.put("wardInfo", wardInfo);                                                     //와드 정보
        jsonObject.put("minionKilledInfo",minionKilledInfo);                          //미니언 처치 수 정보
        jsonObject.put("getGameChampName", getGameChampName);          //챔피언 이름
        return jsonObject;
    }

    /**
     * @todo 검색한 유저의 주 라인을 구한다.
     * @param   lane 라인배열
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
            mainLine="TOP";
        }
        if(jungle>=mainLineGame) {
            mainLineGame=jungle;
            mainLine="JUNGLE";
        }
        if(mid>=mainLineGame){
            mainLineGame=mid;
            mainLine="MIDDLE";
        }
        if(bottom>=mainLineGame){
            mainLineGame=bottom;
            mainLine="BOTTOM";
        }
        if(support>=mainLineGame){
            mainLineGame=support;
            mainLine="UTILITY";
        }
        return mainLine;
    }
    /**
     * @todo 유저가 자주하는 역할군을 가져온다.
     * @param myChampName
     * @return String role  (자주하는 챔피언의 역할군)
     */
    public String getRole(ArrayList<String> myChampName){
        String champMapping="";
        String role="";
        String mainRole="";
        int roleGame=0;
        ArrayList<String> championName= new ArrayList<>();
        ArrayList<String> roles = new ArrayList<>();

        String championsURL="https://ddragon.leagueoflegends.com/cdn/13.1.1/data/ko_KR/champion.json";
        //챔피언 정보 API
        JSONArray zero =getAPI("?",championsURL);
        JSONObject data= (JSONObject) zero.get(0);
        JSONObject champions=(JSONObject) data.get("data");
        Iterator<String>c = champions.keySet().iterator();
        while (c.hasNext()) {
            championName.add(c.next().toString());
        }
        //이름을 서로 조회하여 "tags" 를 조회해야함
        for(int i = 0 ; i<championName.size(); i++){
            champMapping=championName.get(i);
            JSONObject champInfo=(JSONObject)champions.get(champMapping);
            for(int j=0; j< myChampName.size(); j++){
                if(champMapping.equals(myChampName.get(j))){
                    JSONArray tagArr=(JSONArray) champInfo.get("tags");
                    for(int cnt=0; cnt<tagArr.size(); cnt++){
                            role=(String)tagArr.get(cnt);
                            roles.add(role);
                    }
                }
            }
        }
//        System.out.println(roles.toString());     //유저가  20경기동안 한 역할군
        int assassin=       Collections.frequency(roles,"Assassin");
        int mage=             Collections.frequency(roles,"Mage");
        int tank=             Collections.frequency(roles,"Tank");
        int support=       Collections.frequency(roles,"Support");
        int fighter=        Collections.frequency(roles,"Fighter");
        int marksMan=  Collections.frequency(roles,"Marksman");

        if(assassin>=roleGame){
            mainRole="assassin";
            roleGame=assassin;
        }
        if(mage>= roleGame){
            mainRole="mage";
            roleGame=mage;
        }
        if(tank>= roleGame){
            mainRole="tank";
            roleGame=tank;
        }
        if(fighter>= roleGame){
            mainRole="fighter";
            roleGame=fighter;
        }
        if(support>= roleGame){
            mainRole="support";
            roleGame=support;
        }
        if(marksMan>= roleGame){
            mainRole="marksMan";
        }
        return mainRole;
    }

    /**
     * @Todo   토탈 데미지 총량을 구한다.
     * @param getMatchInfo  매치정보
     * @param   line 비교할 주 라인
     * @return ArrayList<Integer>  판 당  총합 데미지
     */
    public HashMap<String,ArrayList<Integer>> getTotalDamage(JSONArray getMatchInfo,String line){
        HashMap<String,ArrayList<Integer>> damageInfo=new HashMap<>();
        ArrayList<Integer> totalDamages= new ArrayList<>();
        ArrayList<Integer> damagePerMinute = new ArrayList<>();
        String mainLine="";

        for (int i = 0; i < getMatchInfo.size(); i++) {
            JSONObject userGameInfo = (JSONObject) getMatchInfo.get(i);
            mainLine=(String)userGameInfo.get("lane");                                                        //해당 라인 구하기

            if(mainLine.equals(line)){                                                                                       //해당 라인과 주라인에 대한 계산값 구하기
                                                                                                                                                // ex) 주라인이 탑이라면 탑을 한 경기만 가져온다.
                totalDamages.add((Integer)userGameInfo.get("totalDamages"));                   //해당 라인에 존재한 최종 데미지를 가져온다.
                damagePerMinute.add((Integer)userGameInfo.get("damagePerMinute")); // 분당 데미지 값 을 가져온다.
            }
        }
        damageInfo.put("totalDamages",totalDamages);
        damageInfo.put("damagePerMinute",damagePerMinute);
        return damageInfo;
    }
    /**
     * @Todo 와드 정보를 구한다.
     * @param getMatchInfo 매치정보
     * @Param line 비교할 주 라인
     * @return HashMap<String,ArrayList<Integer>> 와드정보 (와드설치 , 와드 삭제 , 시야점수 )
     */
    public  HashMap<String,ArrayList<Integer>> getWardInfo(JSONArray getMatchInfo,String line){
        ArrayList<Integer> wardPlaced= new ArrayList<>();
        ArrayList<Integer> wardKilled= new ArrayList<>();
        ArrayList<Integer> visionScore = new ArrayList<>();
        HashMap<String,ArrayList<Integer>> wardInfo=new HashMap<>();
        String mainLine="";

        for (int i = 0; i < getMatchInfo.size(); i++) {
            JSONObject userGameInfo = (JSONObject) getMatchInfo.get(i);
            mainLine = (String) userGameInfo.get("lane");                                            //해당 라인 구하기

            if (mainLine.equals(line)) {                                                                             //해당 라인과 주라인에 대한 계산값 구하기
                // ex) 주라인이 탑이라면 탑을 한 경기만 가져온다.
                wardPlaced.add((Integer) userGameInfo.get("wardsPlaced"));        //해당 라인에서 진행한 와드 설치 개수를 가져온다.
                wardKilled.add((Integer) userGameInfo.get("wardsKilled"));         //해당 라인에서 진행한 와드 삭제 개수를 가져온다.
                visionScore.add((Integer) userGameInfo.get("visionScore"));         //해당 라인에서 진행한 시야 점수 를 가져온다.
            }
        }
        wardInfo.put("wardPlaced",wardPlaced);
        wardInfo.put("wardKilled",wardKilled);
        wardInfo.put("visionScore",visionScore);
        return wardInfo;
    }

    /**
     * @Todo 미니언  처치 수를 구한다.
     * @param getMatchInfo 매치정보
     * @Param line 비교할 주 라인
     * return HashMap<String,ArrayList<Integer>> 미니언 처치수
     */
    public HashMap<String,ArrayList<Integer>> getMinionsKilledInfo (JSONArray getMatchInfo,String line){

        HashMap<String,ArrayList<Integer>> minionsKilledInfo = new HashMap<>();         // 미니언 처치 정보
        ArrayList<Integer> minion10Minutes = new ArrayList<>();                                          // 10분 간 미니언  처치 수
        ArrayList<Integer> jungle10Minutes = new ArrayList<>();                                            // 10분 간 정글 몬스터 처치 수
        ArrayList<Integer> total10Minutes = new ArrayList<>();                                               // 10분 간 총 미니언 처치 수
        ArrayList<Integer> totalMinionsKilled = new ArrayList<>();                                         // 총 미니언 처치 수
        ArrayList<Integer> neutralMinionsKilled = new ArrayList<>();                                    // 총 정글몬스터 처치 수
        ArrayList<Integer> gameTotalMinionKilled = new ArrayList<>();                                // 게임 총합 미니언 처치 수
        String mainLine="";
        for(int i =0 ; i<getMatchInfo.size(); i++){
            JSONObject userGameInfo = (JSONObject) getMatchInfo.get(i);
            mainLine = (String) userGameInfo.get("lane");                                                           //해당 라인 구하기
            if(mainLine.equals(line)){
                minion10Minutes.add((Integer)userGameInfo.get("laneMinionsFirst10Minutes"));
                jungle10Minutes.add((Integer)userGameInfo.get("jungleCsBefore10Minutes"));
                total10Minutes.add((Integer)userGameInfo.get("jungleCsBefore10Minutes")+(Integer)userGameInfo.get("laneMinionsFirst10Minutes"));
                totalMinionsKilled.add((Integer)userGameInfo.get("totalMinionsKilled"));
                neutralMinionsKilled.add((Integer)userGameInfo.get("neutralMinionsKilled"));
                gameTotalMinionKilled.add((Integer)userGameInfo.get("totalMinionsKilled")+(Integer)userGameInfo.get("neutralMinionsKilled"));
            }
        }

        minionsKilledInfo.put("laneMinionsFirst10Minutes",minion10Minutes);
        minionsKilledInfo.put("jungleCsBefore10Minutes",jungle10Minutes);
        minionsKilledInfo.put("total10Minutes",total10Minutes);
        minionsKilledInfo.put("totalMinionsKilled",totalMinionsKilled);
        minionsKilledInfo.put("neutralMinionsKilled",neutralMinionsKilled);
        minionsKilledInfo.put("gameTotalMinionKilled",gameTotalMinionKilled);
        return  minionsKilledInfo;
    }

    /**
     * @Todo  챔피언 이름을 구한다.
     *@param getMatchInfo 매치정보
     * @Param line 비교할 주 라인
     * return ArrayList<String> 챔피언 이름
     */
    public HashMap<String,ArrayList<String>> getGameChampName(JSONArray getMatchInfo,String line){
        ArrayList<String> getChampNames = new ArrayList<>();
        ArrayList<String> kda= new ArrayList<>();
        HashMap<String,ArrayList<String>> getChampCount = new HashMap<>();

        String mainLine="";

        for (int i = 0; i < getMatchInfo.size(); i++) {
            JSONObject userGameInfo = (JSONObject) getMatchInfo.get(i);
            mainLine=(String)userGameInfo.get("lane");                                                 //해당 라인 구하기
            if(mainLine.equals(line)){
                getChampNames.add((String)userGameInfo.get("championName"));
                kda.add(Double.toString((Double) userGameInfo.get("kda")));
            }
        }

        for(int i =0 ; i< getChampNames.size(); i++){
            String key=getChampNames.get(i);
            ArrayList<String> KDA= new ArrayList<>();
            if( getChampCount .containsKey(key)){
                int count=Integer.parseInt( getChampCount .get(key).get(0));
                count+=1;
                Double kda1=Double.valueOf( getChampCount .get(key).get(1));
                kda1+=Double.valueOf(kda.get(i));
                KDA.add(Integer.toString(count));
                KDA.add(Double.toString(kda1));
                getChampCount .put(key,KDA);
            }else{
                KDA.add(Integer.toString(1));
                KDA.add(kda.get(i));
                getChampCount .put(key,KDA);
            }
        }
        return getChampCount;
    }

}

