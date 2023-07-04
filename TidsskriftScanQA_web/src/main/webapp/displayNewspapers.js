let configJson;
$.getJSON("api/config.json").done((data) => configJson = data);

/*
* Event handler for note on newspaper level.
* */
function noteNewspaperSubmitHandler(event) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);
    let parts = ["api", data.get('avis'), "notes"];
    let url = parts.join("/");
    const notes = data.get('standardNote') + " " + data.get('notes');

    $.ajax({
        type: "POST", url: url, data: notes, success: function () {
            alert("notes updated");
            location.reload();
        }, dataType: "json", contentType: "application/json"
    });
    return false;  // <- cancel event
}
/*
* Event handler for deleting a note on newspaper level.
* */

function noteNewspaperDeleteHandler(event) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);
    let parts = ["api", data.get('avis'), "notes"];
    let query = new URLSearchParams();

    query.append("id", data.get('id'));

    let url = parts.join("/") + "?" + query.toString();
    const notes = data.get('notes');

    $.ajax({
        type: "DELETE", url: url, data: notes, success: function () {
            alert("note deleted");
            location.reload();
        }, dataType: "json", contentType: "application/json"
    });
    return false;  // <- cancel event
}
function getNewspaperIDs(){
    return new Promise((r) =>{
        $.getJSON('api/newspaperIDs',
            /**
             * @param {NewspaperID[]} newspaperIDs
             */
            function (newspaperIDs){
                r(newspaperIDs);
        });
    });
}

/*
* Loads newspaperID data from API.
* Crates data array for sidebar newspaper tables.
* inactiveNewspaperData is when the newspaper's batches are all either approved or rejected.
* */
async function loadNewspaperIDs() {
        let data = [];
        let inactiveNewspaperData = [];

        for (let newspaperID of await getNewspaperIDs()) {
            let tmp = {};
            tmp['avis'] = newspaperID.avisid;
            tmp['recievedDate'] = newspaperID.deliveryDate;

            if(newspaperID.isInactive){
                inactiveNewspaperData.push(tmp);
            }else{
                data.push(tmp);
            }
        }
        let $table = $("#avisIDer");
        $table.bootstrapTable({
            data: data, columns: [{
                title: 'Avis',
                field: 'avis',
                formatter: function (value) {
                    return `<a href= '#/newspaper/${value}/0/'>${value.length > 20 ? value.substring(0,17)+'...' : value}</a>`;
                },
                sortable: true
            },
                {
                    title: 'Modtaget',
                    field: 'recievedDate',
                    sortable: true
                }
            ]
        });
        let $tableArkiv = $("#avisIDerArkiv");
        $tableArkiv.bootstrapTable({
            data: inactiveNewspaperData, columns: [{
                title: 'Avis',
                field: 'avis',
                formatter: function (value) {
                    return `<a href= '#/newspaper/${value}/0/'>${value.length > 20 ? value.substring(0,17)+'...' : value}</a>`;
                },
                sortable: true
            },
                {
                    title: 'Modtaget',
                    field: 'recievedDate',
                    sortable: true
                }
            ]
        });
}
/*
* Creates display for the whole newspaper.
* */
/**
 * @param {String} avisID
 * @param {Number} year
 */
function loadYearsForNewspaper(avisID, year) {
    const url = `api/years/${avisID}`;
    $.getJSON(url)
        .done(
            /**
             * @param {String[]} years
             */
            function (years) {
                $("#headline-div").empty().append($("<h1>").text(`År med ${avisID}`));
                $("#state-div").empty();
                let $notice = $("#notice-div").empty();
                if (year === 0) {
                    year = parseInt(years.sort()[0]);
                }
                const notesUrl = `api/${avisID}/notes`;
                $.getJSON(notesUrl)
                    .done(
                        /**
                         * @param {Note[]} notes
                         */
                        function (notes) {
                            let $notesButtonDiv = $("<div/>", {id: "notesButtonDiv"});
                            let $notesButton = $("<button/>", {
                                class: `notesButton btn ${notes.length > 0 ? "btn-warning" : "btn-primary"} btn-primary`,
                                text: `${notes.length > 0 ? "Show " + notes.length + " notes and " : ""}create notes`
                            });
                            let $showNotesDiv = $("<div/>", {
                                visible: false,
                                class: `showNotesDiv ${(this.visible == 'true' ? "active" : "")}`,
                                tabindex: "100"
                            });
                            $showNotesDiv.offsetTop = $notesButton.offsetTop;
                            setShowNotesFocusInAndOut($notesButton, $showNotesDiv);

                            let $newspaperNotesForm = $("<form/>", {
                                id: "newspaperNotesForm",
                                action: "",
                                method: "post"
                            });
                            const formRow1 = $("<div>", {class: "form-row form-row-upper"})
                            const formRow2 = $("<div>", {class: "form-row form-row-lower"})
                            $newspaperNotesForm.append(formRow1);
                            $newspaperNotesForm.append(formRow2);

                            let $newspaperDropDown = $("<select/>", {
                                class: "form-control calendarNotesDropdown", name: "standardNote"
                            });
                            formRow1.append($newspaperDropDown);
                            $newspaperDropDown.append($("<option>", {
                                class: "",
                                value: "",
                                html: "",
                                selected: "true"
                            }));
                            for (const option of configJson.newspaper.dropDownStandardMessages.options) {
                                $newspaperDropDown.append($("<option>", {
                                    class: "dropdown-item",
                                    value: option,
                                    html: option
                                }));
                            }
                            let $hiddenTextAreaValue = $("<input/>", {type: "hidden", name: "notes"})
                            formRow1.append($hiddenTextAreaValue);
                            formRow1.append($("<span/>", {
                                class: "userNotes calendarNotes", id: "batchNotes", type: "text"
                            }).attr('contenteditable', true).on('input', (e) => {
                                $hiddenTextAreaValue.val(e.target.innerText);
                            }));
                            formRow1.append($("<input/>", {
                                class: "btn btn-sm btn-outline-dark",
                                id: "newspaperNotesFormSubmit",
                                type: "submit",
                                name: "submit",
                                form: "newspaperNotesForm",
                                value: "Gem"
                            }));
                            $newspaperNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: avisID}));
                            $newspaperNotesForm.submit(noteNewspaperSubmitHandler);
                            $showNotesDiv.append($newspaperNotesForm);
                            if (notes) {

                                for (let i = 0; i < notes.length; i++) {
                                    let $newspaperForm = $("<form>", {action: "", method: "delete"});
                                    $newspaperForm.append($("<input/>", {type: "hidden", name: "avis", value: avisID}));

                                    const note = notes[i];
                                    $newspaperForm.append($("<input/>", {type: "hidden", name: "id", value: note.id}));

                                    const formRow = $("<div>", {class: "form-row"});
                                    $newspaperForm.append(formRow);

                                    let $newspaperNote = $("<span/>", {
                                        class: "userNotes",
                                        type: "text",
                                        name: "notes",
                                        text: note.note,
                                        readOnly: "true",
                                        disabled: true
                                    });
                                    formRow.append($("<label/>", {
                                        for: $newspaperNote.uniqueId().attr("id"),
                                        text: `-${note.username} ${moment(note.created).format("DD/MM/YYYY HH:mm:ss")}`
                                    }))
                                    formRow.append($newspaperNote);
                                    formRow.append($("<button/>", {class: "bi bi-x-circle-fill", type: "submit"}).css({
                                        "border": "none",
                                        "background-color": "transparent"
                                    }));
                                    $newspaperForm.submit(noteNewspaperDeleteHandler);
                                    $showNotesDiv.append($newspaperForm);
                                }
                            }
                            $notesButtonDiv.append($notesButton);
                            $notice.append($notesButtonDiv);
                            $notice.append($showNotesDiv);
                        })
                renderNewspaperForYear(years, year, `api/dates/${avisID}/${year}`);
                renderBatchTable(avisID);
            });
}
/**
 * @param {iterable<Number>} years array of years to render (either Generator<int, void, int> or int[])
 * @param {Number} currentyear current year
 * @param {String} url url to get NewspaperDates from
 */
function renderNewspaperForYear(years, currentyear, url) {

    let $primary = $("#primary-show").empty();
    for (const year of years) {
        const link = $("<a/>").attr({
            href: editYearIndexInHash(location.hash, year),
            class: `btn btn-sm btn-outline-secondary ${(year == currentyear ? "active" : "")}`,
        }).text(year);
        $("#year-nav").append(link);
    }

    //See dk.kb.kula190.api.impl.DefaultApiServiceImpl.getDatesForNewspaperYear
    $.getJSON(url)
        .done(function (dates) {
            let datesInYear = splitDatesIntoMonths(dates);
            buildCalendar(currentyear, datesInYear)

        });
}
function renderNewspaperForYear2(years, currentyear, url) {
    const yearNav = $("<div/>", {
        class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap',
        id: 'year-nav'
    });
    let nav = $("<div/>", {
        class: 'btn-toolbar mb-2 mb-md-0'
    }).append(yearNav);

    let $primary = $("#primary-show");
    $primary.html(nav);

    const yearShow = $("<div/>", {id: 'year-show'});
    const heading = $("<h1/>", {text: "show me a newspaper"});
    yearShow.append(heading);
    $primary.append(yearShow);

    for (const year of years) {
        const link = $("<a/>").attr({
            href: editYearIndexInHash(location.hash, year),
            class: `btn btn-sm btn-outline-secondary ${(year == currentyear ? "active" : "")}`,
        }).text(year);
        $("#year-nav").append(link);
    }

    //See dk.kb.kula190.api.impl.DefaultApiServiceImpl.getDatesForNewspaperYear
    // var url = 'api/dates/' + newspaper + '/' + currentyear;
    $.getJSON(url)
        .done(function (dates) {
            let datesInYear = splitDatesIntoMonths(dates);
            // $("#year-show").load("calendarDisplay.html", async function () {
            for (let i = 0; i < datesInYear.length; i++) {
                // const calElem = "#month" + i;
                // let datesInYearElement = datesInYear[i];
                // let html = "<h3>" + datesInYearElement.name + "</h3>";
                buildCalendar(currentyear, datesInYearElement.days);
                    // $(calElem).html(html);
                }
            // });
        });
}
/**
 *
 * @param { jQuery|HTMLElement } element
 * @param {number} noteCount
 * Depending on batch state, or some conditions
 * The styling on the element are changed
 * Usage on newspaper, batch calendar and edition page buttons.
 */
function determineColor(dayInMonth, noteCount) {
    //TODO Color code if no content on day
    dayInMonth.problems = dayInMonth.problems == null ? "PROBLEMS" : dayInMonth.problems
    if (dayInMonth.state === "") {
        return configJson.global.calendarStyling.notWithinBatch;
    } else if (dayInMonth.problems.length > 0) {
        return configJson.global.calendarStyling.error;
    }
    else {
        return configJson.global.calendarStyling.default;
    }
    if (dayInMonth.state === "APPROVED") {
        return configJson.batch.stateButtonOptions.APPROVED.calendarStyling;
    }
}
function buildCalendar(year, availableDates, numberOfMonths=12){
    let $primary = $("#primary-show");
    let $calendar = $("<div/>",{class:"calendar"})
    $primary.append($calendar);
    let dataSource = [];
    for (let ad in availableDates) {
        for(let d in availableDates[ad].days){
            dataSource.push(
                {
                    batchid: availableDates[ad].days[d].batchid,
                    avisid: availableDates[ad].days[d].avisid,
                    date: availableDates[ad].days[d].day,
                    startDate: availableDates[ad].days[d].day._d,
                    endDate: availableDates[ad].days[d].day._d,
                    color: determineColor(availableDates[ad].days[d],availableDates[ad].days[d].notesCount).backgroundColor
                })

        }
    }
    const calendar = new Calendar(document.querySelector(".calendar"),{
        //TODO Handle multiple years
        startDate: new Date(year.toString()),
        clickDay: openCalendarDay,
        minDate: new Date((year - 1).toString() + "-12-31"),
        maxDate: new Date(year.toString() + "-12-31"),
        style: "background",
        numberMonthsDisplayed: numberOfMonths
    })

    calendar.setDataSource(dataSource)
    console.log(calendar.getDataSource())

}
function openCalendarDay(e){
    //TODO Look into when we got days without content
    if(e.events[0]){
        let batchid = e.events[0].batchid
        let avisid = e.events[0].avisid
        let date = e.events[0].date.format('YYYY-MM-DD')
        location.href = "#/newspapers/" + batchid + "/" + avisid + "/" + date + "/0/0/0/";
    }


}
/**
 * @returns {Promise}
 * */
function getNoteCountForNoEdition(batchID,newspaperID,date){
     return new Promise((r) => {
         $.getJSON(`api/noEditionNoteCount/${newspaperID}/${batchID}/${date}`,
             function (noteCount) {
                 r(noteCount);
             });
     });
}
/**
 *
 * @param {NewspaperDate[] } dates
 * @returns {*[]}
 */
function splitDatesIntoMonths(dates) {
    let
        months = [];
    months[0] = {name: "Januar", days: []};
    months[1] = {name: "Februar", days: []};
    months[2] = {name: "Marts", days: []};
    months[3] = {name: "April", days: []};
    months[4] = {name: "Maj", days: []};
    months[5] = {name: "Juni", days: []};
    months[6] = {name: "Juli", days: []};
    months[7] = {name: "August", days: []};
    months[8] = {name: "September", days: []};
    months[9] = {name: "Oktober", days: []};
    months[10] = {name: "November", days: []};
    months[11] = {name: "December", days: []};

    let d;
    for (d in dates) {
        let newspaperDate = dates[d]; //as [ 1920 , 1 ,2 ] with first month as 1
        let date = newspaperDate.date;
        date[1] -= 1; //javascript uses 0-indexed months, so adapt
        let day = moment(date);
        months[day.month()].days.push({
            "day": day,
            "count": newspaperDate.pageCount,
            "editionCount": newspaperDate.editionCount,
            "state": newspaperDate.state,
            "problems": newspaperDate.problems,
            "notesCount": newspaperDate.notesCount,
            "batchid": newspaperDate.batchid,
            "avisid": newspaperDate.avisid
        });
    }
    return months;
}

/**
 * @param {String} origHash
 * @param {String} newIndex
 * @returns {String}
 */
function editYearIndexInHash(origHash, newIndex) {
    let hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newIndex; // there's an empty place..
    return hashParts.join("/");
}
