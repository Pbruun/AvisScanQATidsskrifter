function renderTechnicalDisplay(){
    let primaryDisplay = $("#primary-show").empty();
    let now = new Date(Date.now())
    let month = now.getMonth();
    let year = now.getFullYear();
    //const url = `api/batch/${month}/${year}`;
    const url = `api/batches/12/2022`;
    $.get(url)
        .done(
            function (batches){
                console.log(batches)
                buildCalendarMonth(2022,12, batches)
            }
        )
    const url2 = `api/batch/12/2022`;
}

function buildCalendarMonth(year, month,batches){
    let $primary = $("#primary-show");
    let $calendar = $("<div/>",{class:"monthlyCalendar"})
    $primary.append($calendar);
    let dataSource = [];
    let numProblems = 0;
    let avisIds = []
    for (let ad in batches) {
        avisIds.push(batches[ad].avisid)
        numProblems += batches[ad].numProblems;
    }
    const tooltip = $("<p/>",{class:"tooltip"});
    dataSource.push({
        batchid: batches[0].batchid,
        date: new Date(batches[0].startDate),
        startDate: new Date(batches[0].startDate),
        endDate: new Date(batches[0].endDate),
        numProblems:numProblems,
        avisIds:avisIds,
        color: determineColor(batches[0],dataSource.numNotes).backgroundColor,
        tooltipContent: `${batches[0].batchid}\n${numProblems}`
    })
    const calendar = new Calendar(document.querySelector(".monthlyCalendar"),{
        startDate: new Date(`${(year).toString()}-${(month).toString()}-1`),
        clickDay: openCalendarDay,
        style: "background",
        numberMonthsDisplayed: 1,
        width: '100%',
        height: '5em',
        mouseOnDay:()=>{
            if(event.events.length > 0){
                const $tooltipDiv = $("<div>",{class:"tooltipDiv"})
                $tooltipDiv.append(tooltip);
                $(event.element).append($tooltipDiv);
                tooltip.text(event.events[0].batchid).append(event.events[0].numProblems)
                //tooltip.text(event.events[0].numProblems)
                tooltip.css("display",'block');
                console.log(tooltip)
            }


        },
        mouseOutDay:()=>{
            console.log("mouseOut")
            tooltip.css("display",'none');
        }

    })
    //console.log(dataSource)
    calendar.setDataSource(dataSource)
    console.log(calendar.getDataSource());
}