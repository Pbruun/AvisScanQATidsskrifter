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
            $("#year-show").load("calendarDisplay.html", async function () {
                for (let i = 0; i < datesInYear.length; i++) {
                    const calElem = "#month" + i;
                    let datesInYearElement = datesInYear[i];
                    let html = "<h3>" + datesInYearElement.name + "</h3>";
                    html += await buildCalendar(currentyear, (i + 1), datesInYearElement.days);
                    $(calElem).html(html);
                }
            });
        });
}
/**
 * @param { {state: string, problems: string, count: number} } dayInMonth
 * @param { jQuery|HTMLElement } element
 * @param {number} noteCount
 * Depending on batch state, or some conditions
 * The styling on the element are changed
 * Usage on newspaper, batch calendar and edition page buttons.
 */
function determineColor(dayInMonth, element, noteCount) {
    if (dayInMonth.state === "") {
        element.css(configJson.global.calendarStyling.notWithinBatch);
    } else if (dayInMonth.problems.length > 0) {
        element.css(configJson.global.calendarStyling.error);
    } else if (dayInMonth.count === 0) {
        element.css(configJson.global.calendarStyling.noPageWithin);
    } else if (dayInMonth.state) {
        if (dayInMonth.state === "APPROVED") {
            element.css(configJson.global.calendarStyling.default);
        } else {
            element.css(configJson.batch.stateButtonOptions[dayInMonth.state].calendarStyling);
        }

    } else {
        element.css(configJson.global.calendarStyling.default);
    }
    if (dayInMonth.state === "APPROVED") {
        element.css(configJson.batch.stateButtonOptions.APPROVED.calendarStyling);
    }
    if (noteCount > 0) {
        element.css(configJson.global.calendarStyling.containsNotes);
    }
}
/**
 * @param {number} year
 * @param {number} month
 * @param {NewspaperDate[]} availableDates
 * @returns {string}
 */
async function buildCalendar(year, month, availableDates) {


    let firstDayOfThisMonth = moment(year + "-" + month + "-01", "YYYY-MM-DD");
    let daysInMonth = [];
    let firstWeekdayOfMonth = firstDayOfThisMonth.weekday();
    let d = moment(firstDayOfThisMonth);
    for (let i = 0; i < firstDayOfThisMonth.daysInMonth(); i++) {
        //Fill in all dates in the calender
        daysInMonth.push({
            day: moment(d),
            available: false,
            count: 0,
            editionCount: 0,
            state: "",
            problems: "",
            notesCount: 0,
            avisid: "",
            batchid: ""
        });
        d.add(1, 'days');
    }
    for (let availableDate of availableDates) {
        //overwrite days where we have content
        let element = daysInMonth[availableDate.day.date() - 1];
        element.available = true;
        element.count = availableDate.count;
        element.editionCount = availableDate.editionCount;
        element.state = availableDate.state;
        element.problems = availableDate.problems;
        element.notesCount = availableDate.notesCount;
        element.avisid = availableDate.avisid;
        element.batchid = availableDate.batchid;
    }

    let calHtml = "";
    calHtml += "<div class='row weekDayLetters'>";
    let weekDayLetters = ['M', 'T', 'O', 'T', 'F', 'L', 'S'];
    for (let i = 0; i < weekDayLetters.length; i++) {
        calHtml += "<div class='col-sm-1'>" + weekDayLetters[i] + "</div>";
    }
    calHtml += "</div>";
    if (firstWeekdayOfMonth > 0) {

        calHtml += "<div class='row'>";
        for (let i = 0; i < firstWeekdayOfMonth; i++) {
            calHtml += "<div class='col-sm-1'>&nbsp;</div>";
        }
    }
    for (let d = 0; d < daysInMonth.length; d++) {
        let colIdx = (firstWeekdayOfMonth + d) % 7;
        if (colIdx === 0) {
            if (d !== 0) {
                calHtml += "</div>";
            }
            calHtml += "<div class='row'>";
        }
        let dayInMonth = daysInMonth[d];
        let date = ("0" + dayInMonth.day.date());
        let month = ("0" + dayInMonth.day.month());
        //Ensure same width of date numbers
        date = date.substring(date.length - 2);
        month = month.substring(date.length - 2);
        if (dayInMonth.available && dayInMonth.editionCount === 0) {
            let noEditionDate = `${dayInMonth.day.format('YYYY-MM-DD')}`;
            dayInMonth.notesCount = await getNoteCountForNoEdition(dayInMonth.batchid, dayInMonth.avisid, noEditionDate);
        }
        calHtml += "<div class='col-sm-1' >";

        let button;
        if (dayInMonth.available) {
            button = $("<a/>", {
                href: "#/newspapers/" + dayInMonth.batchid + "/" + dayInMonth.avisid + "/" + dayInMonth.day.format('YYYY-MM-DD') + "/0/0/0/",
                title: dayInMonth.count + " page(s) \n" + dayInMonth.editionCount + " edition(s)\n" + dayInMonth.notesCount + " note(s)"
            });
        } else {
            button = $("<button/>", {
                type: 'button'
            });
        }
        button.attr("style", 'padding-left: 0; padding-right: 0')
            .text(date)
            .addClass("btn btn-sm");
        determineColor(dayInMonth, button, dayInMonth.notesCount);
        calHtml += button.prop('outerHTML');
        calHtml += "</div>";
    }
    calHtml += "</div>";
    return calHtml;
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
