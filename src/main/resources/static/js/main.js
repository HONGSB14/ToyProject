/**
*   @todo:입력값
*/
function idInput() {
    let html="";
    let status="";
    $.ajax({
            url:"/user/idInput",
            data:{"myId":$("#myId").val()},
            method:'POST',
            success:function (data){
                   console.log(data);
                    if(data!=false){
                            userInfo(data);
                            status="error";
                            if(RankInfo(data)!=status){
                                RankInfo(data);
                                MostChampion(data);
                                lineInfo(data);
                            }else{
                                outPutErr();
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

    const oddUpper="대리 주의보!!<br> 몸을 사려야합니다! 주의하세요. ";                                                           //승률이 60% 이상
    const oddMMU="생각보다  훨씬 어려운 상대입니다.<br> 팀원의 도움을 받으세요.";                                      //승률이 55~59%
    const oddMU="평타이상 칩니다.<br> 절대 방심하지마세요.";                                                                            //승률이 50~55%
    const oddMiddle="나사가 하나 빠졌습니다. <br>빠르게 파악 후<br> 나사빠진 부분을 공략해야합니다.";  //승률이 45~49%
    const oddML="방심만 하지 않는다면 <br>무난하게 이길 수 있습니다.";                                                          //승률이 42~44%
    const oddLow="솔직히 만만한 상대입니다. <br>지면 자신을 되돌아보세요.";                                                //승률이 41%이하
    let html= "";
    let soloQueueType="RANKED_SOLO_5x5";         // 큐타입
    let status="error";                                                     //존재하지 않는 소환사 상태값
    let myInfo="";                                                            //전체적인 랭크정보 (솔랭,팀랭)
    let tier="";                                                                   //자신의 티어
    let point="";                                                                //리그 포인트
    let wins="";                                                                 //승리 횟수
    let losses="";                                                               //패배 횟수
    let odds="";                                                                 //승률
    let oddComment="";                                                 //승률 코멘트
    myInfo=data[1].myInfo;
    $("#userRankInfo").empty();
    $("#mainStatusInfo").empty();
    $("#mostChamp").empty();
    $("matchDataInfo").empty();
    if(myInfo.length ==0) return status;
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
                                    '<h5 class="offset-1"><img src="../img/rank_icon/Emblem_'+tier.split(" ")[0]+'.gif" style="width:85px; height:85px; border-radius:70%;"></h5>'+
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
                                '<h2><strong>Comment</strong></h2>'+
                                      '<div class="col-md-12 p-5">'+
                                                '<h3><strong>'+oddComment+'</strong></h3>'+
                                        '</div>'+
                                '</div>';
            $("#userRankInfo").append(html);
        }
    }
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
                                     '<img src="../img/champion/'+champName+'.png"  width="50px" height="50px" style="border-radius: 100%;">'+
                            '</div>'+
                            '<div class="col-md-4 p-1">'+
                                    '<h5><i>'+champName_kr+'</i></h5>'+
                                    '<h6 style="color:"#6E6E6E;"><i>'+champTitle+'</i></h6>'+
                            '</div>'+
                        '</div>';
    }
    $("matchDataInfo").empty();
    $("#mainStatusInfo").empty();
    $("#mostChamp").empty();
    $("#mostChamp").append(mostInfoHtml);
    $("#mostChamp").append(html);
}


/**
* @todo 유저의 주 라인을 나타낸다.
*/
function lineInfo(data){

      $("matchDataInfo").empty();
      let line=data[3].lane;
      if(line=="top") line=" 탑에 죽고 탑에 사는 진정한 탑 그 잡채 이시군요 !";
      else if(line=="jungle") line="팀원들을 장기판으로 쓰는 그 잡채 정글러 이시군요 !";
      else if(line=="mid") line="당신이 있어 팀이 존재합니다. 미드 그 잡채 이시군요!";
      else if(line=="bottom") line="후반에 가장 중요한 핵심 그 잡채 원딜러 !";
      else if(line=="support") line="입벌려 킬 들어간다. 서포터 그잡채 !";
      else line="진정한 올 라운더 그 잡채 !";

      html="";
      html+='<div class="col-md-12">'+
                        '<h2><strong>Data Analysis</strong></h2>'+
                    '</div>'+
                   '<h3><i>'+line+'</i></h3>';
      $("#matchDataInfo").append(html);
}




























/**
*   @todo 에러화면을  나타낸다.
*   @return 에러화면
*/
function outPutErr(){
    let html="";
     $("#mainStatusInfo").empty();
     $("#userRankInfo").empty();
     $("#mostChamp").empty();
     $("matchDataInfo").empty();
         html+=  '<div class="col-md-12">'+
                             '<h2>데이터 분석실패<h2>'+
                              '<h2>데이터를 불러 올 수 없습니다.<h2>'+
                              '<h6>검색하신 아이디를 다시한번 확인해주세요.</h6>'+
                         '</div>';
    $("#mainStatusInfo").append(html);
}
