$(function() {
    $("#loading").hide();   //스피너 숨기기
});
/**
*   @todo:입력값
*/
function idInput() {
    let html="";
    let status="error";       //error 발생
    let lane="";                    // 주 라인
    pageClear();
    $("#loading").show();
    $.ajax({
            url:"/user/idInput",
            data:{"myId":$("#myId").val()},
            method:'POST',
            success:function (data){
                    console.log(data);
                    if(data!=false){
                            if(RankInfo(data)!=status){
                                $("#loading").hide();   //스피너 숨기기
                                userInfo(data);
                                MostChampion(data);
                                lane=lineAndRole(data);
                                if(lane == "Top"){
                                     totalDamage(data);
                                }else if (lane == "Jungle"){
                                     totalDamage(data);
                                }else if (lane =="Mid") {
                                     totalDamage(data);
                                }else if (lane == "Bot"){
                                     totalDamage(data);
                                }else if (lane == "Support"){
                                    console.log("서폿");
                                }else {
                                    outPutErr();
                                }
                            }else{
                                outPutGameErr();
                            }
                  }else{
                        outPutErr();
                  }
            }
    });
}
/**
*  @todo 사용자 정보를 가져온다.
*  @param JSONObject
*  @return 내 정보[0] , 상대 정보 [1]
*/
function userInfo(data){
     let myName="";
     myName=data[0].name;
     return myName;
}

/**
*  @todo 나의 랭크정보를 가져온다.
*  @param JSONArray
*  @return 내 랭크 정보
*/
function  RankInfo(data){
    pageClear();
    const oddUpper="대리 주의보!!<br> 몸을 사려야합니다! 주의하세요. ";                                                             //승률이 60% 이상
    const oddMMU="생각보다  훨씬 어려운 상대입니다.<br>자존심을 낮추고<br>팀원의 도움을 받으세요.";        //승률이 55~59%
    const oddMU="평타이상 칩니다.<br> 절대 방심하지마세요.";                                                                             //승률이 50~55%
    const oddMiddle="나사가 하나 빠졌습니다. <br>빠르게 파악 후<br> 나사빠진 부분을 공략해야합니다.";         //승률이 45~49%
    const oddML="방심만 하지 않는다면 <br>무난하게 이길 수 있습니다.";                                                              //승률이 42~44%
    const oddLow="만만한 상대입니다. <br>패배 시 자신을 되돌아보세요.";                                                            //승률이 41%이하
    let html= "";
    let soloQueueType="RANKED_SOLO_5x5";            // 큐타입
    let myInfo="";                                                            //전체적인 랭크정보 (솔랭,팀랭)
    let tier="";                                                                   //자신의 티어
    let point="";                                                                //리그 포인트
    let wins="";                                                                 //승리 횟수
    let losses="";                                                               //패배 횟수
    let odds="";                                                                 //승률
    let oddComment="";                                                 //승률 코멘트
    let status="error";
    myInfo=data[1].myInfo;
    if( myInfo.length == 0 ) return status;
    for(let i=0; i<myInfo.length; i++){
        if(myInfo[i].queueType==soloQueueType){
            tier=myInfo[i].tier;
            tier+=" "+myInfo[i].rank;
            point=myInfo[i].leaguePoints;
            wins=myInfo[i].wins;
            losses=myInfo[i].losses;
            odds=(wins/(wins+losses)*100).toFixed(2);
            if(odds>=60){
                oddComment=oddUpper;
            }else if(odds>=55 && odds<60){
                oddComment=oddMMU;
            }else if(odds>=50 && odds<55){
                oddComment=oddMU;
            }else if(odds>=45 && odds<50){
                oddComment=oddMiddle;
            }else if(odds>=41 && odds<45){
                oddComment=oddML;
            }else if(odds<41){
                oddComment=oddLow;
            }else{
                oddComment="승률을 집계 할 수 없습니다. 관리자에게 문의해주세요.";
            }
            html+= '<div class="col-md-12 row">'+
                                '<h2><strong>Rank Info</strong></h2>'+
                                '<div class="col-md-3">'+
                                    '<h5 class="offset-1"><img class="img-fluid"  src="../img/rank_icon/Emblem_'+tier.split(" ")[0]+'.gif" style="width:85px; height:85px; border-radius:70%;"></h5>'+
                                '</div>'+
                                '<div class="col-md-5 p-3">'+
                                     '<h4><strong>'+tier+'</strong></h4>'+
                                     '<h6>'+point+' LP</h6>'+
                                '</div>'+
                                '<div class="col-md-4 p-3">'+
                                    '<h6>승률  '+odds+'%</h6>'+
                                    '<h6>'+wins+'승 '+losses+'패</h6>'+
                                '</div>'+
                           '</div>'+
                                '<div class="col-md-12">'+
                                '<h2 class="py-2"><strong>Comment</strong></h2>'+
                                      '<div class="offset-3 col-md-9 py-5">'+
                                                '<h3 class="py-3"><strong>'+oddComment+'</strong></h3>'+
                                        '</div>'+
                                '</div>';
            $("#userRankInfo").append(html);
            return          // for 문 탈출
        }
    }
    return status;
}

/**
*  @todo 모스트챔피언을 가져온다.
*  @param JSONArray
*  @return 모스트챔피언
*/
function MostChampion(data){
    let html="";
    let mostInfoHtml="";
    let champName="";
    let champName_kr="";
    let champions=data[2].myMostTop;
    mostInfoHtml+='<div class="col-md-12">'+
                                    '<h2><strong>Most Champions</strong></h2>'+
                                '</div>';
    for(let i = 0 ; i<champions.length; i++){
        champName=champions[i].id;
        champTitle=champions[i].title;
        champName_kr=champions[i].name;
         html+='<div class="col-md-12 row">'+
                            '<div class="col-md-2 p-1">'+
                                     '<img class="img-fluid"  src="../img/champion/'+champName+'.png"  width="50px" height="50px" style="border-radius: 100%;">'+
                            '</div>'+
                            '<div class="col-md-4 p-1">'+
                                    '<h5><i>'+champName_kr+'</i></h5>'+
                                    '<h6 style="color:"#6E6E6E;"><i>'+champTitle+'</i></h6>'+
                            '</div>'+
                        '</div>';
    }
    $("#mostChamp").append(mostInfoHtml);
    $("#mostChamp").append(html);
}

/**
* @todo 유저의 주 라인과 자주쓴 역할군을 나타낸다.
*/
function lineAndRole(data){

      let line=data[3].lane;
      let tag=data[3].champRole;
      let lane="";
      let role=tag;
      let myInfo=data[1].myInfo;
      let tier="";
      let soloQueueType="RANKED_SOLO_5x5";
      for(let i=0; i<myInfo.length; i++){
              if(myInfo[i].queueType==soloQueueType){
                    tier=myInfo[i].tier;
              }
      }
      if(line=="TOP") {
            line="탑";
            lane="Top";
      }
      else if(line=="JUNGLE"){
            line="정글";
            lane="Jungle";
      }
      else if(line=="MIDDLE") {
            line="미드";
            lane="Mid";
      }
      else if(line=="BOTTOM") {
            line="원딜";
            lane="Bot";
      }
      else if(line=="SUPPORT"){
            line="서포터";
            lane="Support";
      }
      else {
            line="올 라운더";
      }

       if(tag=="assassin")                role="암살자";
       else if(tag=="mage")             role="마법사";
       else if(tag=="tank")               role="탱커";
       else if(tag=="fighter")          role="브루저";
       else if(tag=="support")        role="유틸";
       else if(tag=="marksMan")   role="원거리 딜러";
       else                                          role="unknown";

      html="";
      html+='<div class="col-md-12 text-center">'+
                        '<h2 class="m-3"><strong>Data Analysis</strong></h2>'+
                    '</div>'+
                    '<div class="row">'+
                        '<div class="offset-6 col-md-1 px-5">'+
                            '<img  src="../img/rank_position/Position_'+tier+'-'+lane+'.png" width="35px" height="35px">'+
                        '</div>'+
                        '<div class="col-md-5">'+
                            '<h3><i>'+line+' 라인 데이터를 확인하세요.</i></h3>'+
                            '<span><i> 주로 한 챔피언 타입은　</i></span><span style="color:#5941A9"><i>'+role+'</i></span><span><i>　입니다.</i></span>'+
                        '</div>'+
                    '</div>';
      $("#matchDataInfo").append(html);
      return lane;
}

function totalDamage(data) {
    let totalDamages=[];
    let labels=[];
    let reg=/\B(?=(\d{3})+(?!\d))/g;
    let lane="";
    let html="";
    let lowDamage=0;
    let upperDamage=0;
    let deals=0;
    let dataOutputGame=data[3].dataOutputGame;
    lane=data[3].lane;
    totalDamages=data[3].totalDamage;
    upperDamage=totalDamages[0];
    lowDamage=totalDamages[0];
    for(let i=0; i<totalDamages.length; i++){
        if(totalDamages[i]>upperDamage){
            upperDamage=totalDamages[i];
        }
        if(totalDamages[i]<lowDamage){
            lowDamage=totalDamages[i];
        }
        deals+=totalDamages[i];
        let number=i+1;
        labels[i]=String(number)+' Game';
    }
    let dps= deals/totalDamages.length;
    html+=
                   '<div class="col-md-6 my-5" id="chart_1">' +
                       '<h5><i>1. 해당 라인 경기당 총 데미지 그래프</i></h5><br>'+
                       '<h6>해당라인 진행 게임 횟수: '+dataOutputGame+' Game</h6>'+
                       '<h6>평균 DPS : '+(Math.round(dps)).toString().replace(reg,',')+' </h6><br>'+
                       '<h6>가장 높은 데미지 : '+upperDamage.toString().replace(reg, ',')+'</h6>'+
                       '<h6>가장 낮은 데미지 : '+lowDamage.toString().replace(reg, ',')+'</h6>'+
                       '<canvas id="totalDamageChart"></canvas>' +
                   '<div>';
    $("#charts").append(html);
    const ctx=document.getElementById('totalDamageChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
          datasets: [{
            label: totalDamages.length+' game totalDamage',
            data: totalDamages,
            borderWidth: 1
          }]
        },
        options: {
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
}



























/**
*   @todo 에러화면을  나타낸다.
*   @return 에러화면
*/
function outPutErr(){
    let html="";
         html+=  '<div class="col-md-12">'+
                             '<h2>데이터 분석실패<h2>'+
                              '<h2>데이터를 불러 올 수 없습니다.<h2>'+
                              '<h6>검색하신 아이디를 다시한번 확인해주세요. </h6>'
                         '</div>';
    $("#loading").hide();   //스피너 숨기기
    $("#mainStatusInfo").append(html);
}

/**
*   @todo 게임 판수 부족 시 에러화면을  나타낸다.
*   @return 에러화면
*/
function outPutGameErr(){
    pageClear();
    let html="";
         html+=  '<div class="col-md-12">'+
                             '<h2>데이터 분석실패<h2>'+
                              '<h2>데이터를 불러 올 수 없습니다.<h2>'+
                              '<h6>솔로랭크 게임 기준 10판 이상 게임을 진행했는지 확인해 주세요. </h6>'
                         '</div>';
    $("#loading").hide();   //스피너 숨기기
    $("#mainStatusInfo").append(html);
}

/**
* @todo 모든 값을 초기화 시킨다.
*/
function pageClear(){
      $("#loading").hide();   //스피너 숨기기
      $("#userRankInfo").empty();
      $("#mainStatusInfo").empty();
      $("#mostChamp").empty();
      $("#matchDataInfo").empty();
      $("#charts").empty();
}
