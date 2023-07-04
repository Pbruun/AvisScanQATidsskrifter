let editionJsonData;
/** @type {NewspaperDay} NEWSPAPERDATE*/
let NEWSPAPERDATE;
let EDITIONINDEX;
let SECTIONINDEX;
let PAGEINDEX;
/**
 * Creates the top row for the edition display
 * @param { String } batchID
 * @param {string} avisID
 * @param {*} date
 * @param {number} editionIndex
 * @param {number} sectionIndex
 * @param {number} pageIndex
 */
function loadEditionsForNewspaperOnDate(batchID, avisID, date, editionIndex, sectionIndex, pageIndex) {
    $.getJSON("api/config.json").done(function (data) {
        editionJsonData = data.edition
    })
    loadNewspaperDay(batchID,avisID,date,renderBatchButtons,editionIndex,sectionIndex,pageIndex);
    // $.getJSON(url)
    //     .done(
    //         /**
    //          * @param {NewspaperDay} newspaperDay
    //          * */
    //         function (newspaperDay) {
    //
    //         })
    //     .fail(function (jqxhr, textStatus, error) {
    //         $headline.append($("<h1/>").text(`${jqxhr.responseText}`));
    //     });
}
/**
 * Creates Back to newspaper year and back to batch buttons. And creates headline for the newspaper.
 * @param {NewspaperDay} newspaperDay
 * @param {number} editionIndex
 * @param {number} sectionIndex
 * @param {number} pageIndex
 */
function renderBatchButtons(newspaperDay,editionIndex,sectionIndex,pageIndex){
    let day = moment(newspaperDay.date).format('YYYY-MM-DD');
    let nextDay = moment(newspaperDay.date).add(1, 'd').format("YYYY-MM-DD")
    let pastDay = moment(newspaperDay.date).subtract(1, 'd').format("YYYY-MM-DD")
    $("#notice-div").empty();
    $("#state-div").empty();
    $("#primary-show").empty();

    const $headline = $("#headline-div").empty();
    $headline.append($("<a/>", {
        class: "btn btn-secondary",
        text: "Back to newspaper year",
        href: `#/newspaper/${newspaperDay.batch.avisid}/${day.split('-')[0]}/`
    }))
    $headline.append($("<a/>", {
        class: "btn btn-secondary", text: "Back to batch", href: `#/batch/${newspaperDay.batch.batchid}/`
    }))
    let $buttonForward = $("<a/>", {
        class: "btn btn-secondary bi bi-caret-right",
        href: `#/newspapers/${newspaperDay.batch.batchid}/${newspaperDay.batch.avisid}/${nextDay}/0/0/0/`
    }).css({"float": "right"})
    if (day === newspaperDay.batch.endDate) {

        $buttonForward.css({
            "background-color": "#6c757d9e",
            "border-color": "#6c757d9e",
            "pointer-events": "none"
        })
    }
    $headline.append($buttonForward)
    let $buttonBack = $("<a/>", {
        class: "btn btn-secondary bi bi-caret-left",
        href: `#/newspapers/${newspaperDay.batch.batchid}/${newspaperDay.batch.avisid}/${pastDay}/0/0/0/`
    }).css({"float": "right"})
    if (day === newspaperDay.batch.startDate) {
        $buttonBack.css({
            "background-color": "#6c757d9e",
            "border-color": "#6c757d9e",
            "pointer-events": "none"
        })
    }
    $headline.append($buttonBack)
    $headline.append($("<h1>").text(`Editions for ${newspaperDay.batch.avisid} on ${day}`));
    renderDayDisplay(newspaperDay, editionIndex, sectionIndex, pageIndex);
}
/**
 *
 * @param batchID
 * @param avisID
 * @param date
 * @param {function} renderFunction
 * @param editionIndex
 * @param sectionIndex
 * @param pageIndex
 */
function loadNewspaperDay(batchID,avisID,date, renderFunction, editionIndex, sectionIndex, pageIndex){
    let day = moment(date).format('YYYY-MM-DD');
    let url = `api/batch/${batchID}/${avisID}/${day}`
    $.getJSON(url)
        .done(
            /**
             * @param {NewspaperDay} newspaperDay
             * */
            function (newspaperDay) {
                renderFunction(newspaperDay,editionIndex,sectionIndex,pageIndex)
            })
        .fail(function (jqxhr, textStatus, error) {
            const $headline = $("#headline-div").empty();
            $headline.append($("<h1/>").text(`${jqxhr.responseText}`));
        })
}


function noteSubmitHandler(event, url) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);
    let batchID = data.get('batch')
    let parts = ["api", "notes", batchID]
    let query = new URLSearchParams();
    query.append("avis", data.get('avis'));
    query.append("date", data.get('date'));
    query.append("edition", data.get('edition'));
    query.append("section", data.get('section'));
    query.append("page", data.get('page'));

    url = parts.join("/") + "?" + query.toString();
    const notes = data.get('standardNote') + " " + data.get('notes');
    $.ajax({
        type: "POST", url: url, data: notes, success: function () {
            alert("Note added");
            loadNewspaperDay(NEWSPAPERDATE.batch.batchid,NEWSPAPERDATE.batch.avisid,NEWSPAPERDATE.date,renderNotes,EDITIONINDEX,SECTIONINDEX,PAGEINDEX);
        }, dataType: "json", contentType: "application/json"
    });
    return false;  // <- cancel event
}

function noteDeleteHandler(event) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);

    let parts = ["api", "notes", data.get('batch')]
    let query = new URLSearchParams();
    let noteID = data.get('id');
    query.append("id", noteID);

    // let url = parts.filter(x => x).join("/")
    let url = parts.join("/") + "?" + query.toString();

    const notes = data.get('notes');

    $.ajax({
        type: "DELETE", url: url, data: notes, success: function () {
            alert("note deleted")
        }, dataType: "json", contentType: "application/json"
    });
    $(`#noteRow${noteID}`).remove();
    return false;  // <- cancel event
}


function initComponents() {
    let $primary = $("#primary-show");
    const $contentCol = $("<div/>", {class: "col-3", id: "contentCol"});
    const $dayRow = $("<div/>", {id: "dayRow", class: "row"});
    const $editionRow = $("<div/>", {id: "editionRow", class: "row"});
    const $pageRow = $("<div/>", {id: "pageRow", class: "row"});
    const $sectionRow = $("<div/>", {id: "sectionRow", class: "row"});
    $contentCol.append($dayRow);
    $contentCol.append($editionRow);
    $contentCol.append($sectionRow);
    $contentCol.append($pageRow);
    $primary.append($contentCol);
}
/**
 * Creates notes display for date and newspaper
 * @param {NewspaperDay} newspaperDay
 * @param {number} editionIndex
 * @param {number} sectionIndex
 * @param {number} pageIndex
 */
function renderNotes(newspaperDay, editionIndex, sectionIndex, pageIndex){
    $("#dayRow").empty();
    $("#editionRow").empty();
    $("#pageRow").empty();
    $("#sectionRow").empty();
    renderDayNotes(newspaperDay);
    renderEditionNotes(newspaperDay,editionIndex);
    renderSectionsNotes(newspaperDay.editions[editionIndex],sectionIndex)
    renderPageNotes(newspaperDay.editions[editionIndex].sections[sectionIndex].pages[pageIndex])
}
function renderDayNotes(newspaperDay){
    NEWSPAPERDATE = newspaperDay;
    let $hiddenTextAreaValue = $("<input/>", {type: "hidden", name: "notes"});
    const $dayRow = $("#dayRow");
    let $dayNotesTextArea = $("<span/>", {
        class: "userNotes", id: "dayNotes", type: "text"
    }).attr('contenteditable', true).on('input', (e) => {
        $hiddenTextAreaValue.val(e.target.innerText);
    })

    let $dayNotesForm = $("<form>", {id: "dayNotesForm", action: "", method: "post"});

    const formRow1 = $("<div>", {class: "form-row"});
    const formRow2 = $("<div>", {class: "form-row"});
    $dayNotesForm.append(formRow1);
    $dayNotesForm.append(formRow2);


    let $dropDownDayNotes = $("<select/>", {class: "form-select", name: "standardNote"});

    $dropDownDayNotes.append($("<option>", {value: "", html: "", selected: "true"}));
    for (let option of editionJsonData.dropDownStandardMessage.dayDropDown.options) {
        $dropDownDayNotes.append($("<option>", {value: option, html: option}));
    }

    formRow1.append($dropDownDayNotes);
    formRow1.append($("<label/>", {for: "dayNotes"}).text("Day notes"));
    formRow2.append($hiddenTextAreaValue);
    formRow2.append($dayNotesTextArea);
    formRow2.append($("<input/>", {
        id: "dayNotesFormSubmit", type: "submit", name: "submit", form: "dayNotesForm", value: "Gem"
    }));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: newspaperDay.batch.batchid}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: newspaperDay.batch.avisid}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "date", value: newspaperDay.date}));
    $dayNotesForm.submit(noteSubmitHandler);
    $dayRow.append($dayNotesForm);

    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < newspaperDay.notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(newspaperDay.batch.batchid, newspaperDay.notes[i]));
    }
    $dayRow.append($noteContainer);
}
function renderEditionNotes(newspaperDay, editionIndex){
    EDITIONINDEX = editionIndex;
    let editions = newspaperDay.editions;
    const edition = editions[editionIndex];
    const $editionRow = $("#editionRow");
    let $hiddenTextAreaValue = $("<input/>", {type: "hidden", name: "notes"})
    let $editionNotesTextArea = $("<span/>", {
        class: "userNotes", id: "editionNotes", type: "text"
    }).attr('contenteditable', true).on('input', (e) => {
        $hiddenTextAreaValue.val(e.target.innerText);
    })

    let $editionNotesForm = $("<form>", {id: "editionNotesForm", action: "api/editionNotes", method: "post"});

    const formRow1 = $("<div>", {class: "form-row"});
    const formRow2 = $("<div>", {class: "form-row"});
    $editionNotesForm.append(formRow1);
    $editionNotesForm.append(formRow2);

    let $dropDownEditionNotes = $("<select/>", {class: "form-select", name: "standardNote"});

    $dropDownEditionNotes.append($("<option>", {value: "", html: "", selected: "true"}));
    for (let option of editionJsonData.dropDownStandardMessage.udgDropDown.options) {
        $dropDownEditionNotes.append($("<option>", {value: option, html: option}));
    }
    formRow1.append($dropDownEditionNotes);
    formRow1.append($("<label/>", {for: "editionNotes"}).text("Edition notes"));
    formRow2.append($editionNotesTextArea);
    formRow2.append($hiddenTextAreaValue);
    formRow2.append($("<input/>", {
        id: "editionNotesFormSubmit", type: "submit", name: "submit", form: "editionNotesForm", value: "Gem"
    }));
    $editionNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: edition.batchid}));
    $editionNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: edition.avisid}));
    $editionNotesForm.append($("<input/>", {type: "hidden", name: "date", value: edition.date}));
    $editionNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: edition.edition}));
    $editionNotesForm.submit(noteSubmitHandler);
    $editionRow.append($editionNotesForm);
    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < edition.notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(edition.batchid, edition.notes[i]));
    }
    $editionRow.append($noteContainer);
}
function renderSectionsNotes(edition, sectionIndex) {
    SECTIONINDEX = sectionIndex;
    let $sectionRow = $("#sectionRow")
    let sections = edition.sections;

    let $hiddenTextAreaValue = $("<input/>", {type: "hidden", name: "notes"})
    let $sectionNotesTextArea = $("<span/>", {
        class: "userNotes", id: "sectionNotes", type: "text"
    }).attr('contenteditable', true).on('input', (e) => {
        $hiddenTextAreaValue.val(e.target.innerText);
    });
    let $sectionNotesForm = $("<form>", {id: "sectionNotesForm", action: "api/sectionNotes", method: "post"});

    const formRow1 = $("<div>", {class: "form-row"})
    const formRow2 = $("<div>", {class: "form-row"})
    $sectionNotesForm.append(formRow1);
    $sectionNotesForm.append(formRow2);

    let $dropDownSectionNotes = $("<select/>", {class: "form-select", name: "standardNote"});

    $dropDownSectionNotes.append($("<option>", {value: "", html: "", selected: "true"}));
    for (let option of editionJsonData.dropDownStandardMessage.sectionDropDown.options) {
        $dropDownSectionNotes.append($("<option>", {value: option, html: option}));
    }

    formRow1.append($dropDownSectionNotes)
    formRow1.append($("<label/>", {for: "sectionNotes"}).text("Section notes"));
    formRow2.append($hiddenTextAreaValue)
    formRow2.append($sectionNotesTextArea);
    formRow2.append($("<input/>", {
        id: "sectionNotesFormSubmit", type: "submit", name: "submit", form: "sectionNotesForm", value: "Gem"
    }));
    $sectionNotesForm.append($("<input/>", {
        type: "hidden",
        name: "batch",
        value: sections[sectionIndex].batchid
    }));
    $sectionNotesForm.append($("<input/>", {
        type: "hidden",
        name: "avis",
        value: sections[sectionIndex].avisid
    }));
    $sectionNotesForm.append($("<input/>", {type: "hidden", name: "date", value: sections[sectionIndex].date}));
    $sectionNotesForm.append($("<input/>", {
        type: "hidden",
        name: "edition",
        value: sections[sectionIndex].edition
    }));
    $sectionNotesForm.append($("<input/>", {
        type: "hidden",
        name: "section",
        value: sections[sectionIndex].section
    }));
    $sectionNotesForm.submit(noteSubmitHandler);
    $sectionRow.append($sectionNotesForm);
    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < sections[sectionIndex].notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(edition.batchid, sections[sectionIndex].notes[i]));
    }
    $sectionRow.append($noteContainer)
}
function renderPageNotes(page){
    PAGEINDEX = page.pageNumber-1
    const $pageRow = $("#pageRow");
    let $pageNotesForm = $("<form>", {id: "pageNotesForm", action: "", method: "post"});

    const formRow1 = $("<div>", {class: "form-row"});
    const formRow2 = $("<div>", {class: "form-row"});
    $pageNotesForm.append(formRow1);
    $pageNotesForm.append(formRow2);
    let $standardMessageSelect = $("<select/>", {
        class: "form-select", name: "standardNote"
    }).append($("<option>", {value: "", html: "", selected: "true"}));
    for (let option of editionJsonData.dropDownStandardMessage.pageDropDown.options) {
        $standardMessageSelect.append($("<option>", {
            value: option, html: option
        }));
    }
    formRow1.append($standardMessageSelect);
    formRow1.append($("<label/>", {for: "pageNotes"}).text("Page notes"));

    let $hiddenTextAreaValue = $("<input/>", {type: "hidden", name: "notes"});
    formRow2.append($hiddenTextAreaValue);
    let $pageNotesTextArea = $("<span/>", {
        class: "userNotes", id: "pageNotes", type: "text"
    });
    $pageNotesTextArea.attr('contenteditable', true).on('input', (e) => {
        $hiddenTextAreaValue.val(e.target.innerText);
    });
    formRow2.append($pageNotesTextArea);
    formRow2.append($("<input/>", {
        id: "pageNotesFormSubmit", type: "submit", name: "submit", form: "pageNotesForm", value: "Gem"
    }));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: page.batchid}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: page.avisid}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "date", value: page.editionDate}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: page.editionTitle}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "section", value: page.sectionTitle}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "page", value: page.pageNumber}));

    $pageNotesForm.submit(noteSubmitHandler);
    $pageRow.append($pageNotesForm);
    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < page.notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(page.batchid, page.notes[i]));
    }
    $pageRow.append($noteContainer);

    let $contentCol = $("#contentCol");
    $contentCol.append($pageRow);
}
/**
 * Creates the day column and the edition column with its content
 * @param {NewspaperDay} newspaperDay
 * @param {number} editionIndex
 * @param {number} sectionIndex
 * @param {number} pageIndex
 */
function renderDayDisplay(newspaperDay, editionIndex, sectionIndex, pageIndex) {
    $("#primary-show").empty();
    initComponents();
    let editions = newspaperDay.editions;
    const edition = editions[editionIndex];
    renderDayNotes(newspaperDay)
    if (editionIndex < 0 || editionIndex >= editions.length) {
        return;
    }
    renderEditionNotes(newspaperDay,editionIndex)
    renderSectionsNotes(edition, sectionIndex);
    edition.sections.sort((a, b) => {
        return a.section < b.section ? -1 : 1
    });
    if (edition.sections[sectionIndex].pages.length === 1) {
        renderSinglePage(edition.sections[sectionIndex].pages[0],newspaperDay);
    } else {
        renderSection(edition.sections[sectionIndex], pageIndex,edition.sections,newspaperDay);
    }
}

/**
 * Creates the page column
 * @param {NewspaperPage} page
 * @param {NewspaperDay} newspaperDay
 */
function renderSinglePage(page,sections,newspaperDay) {
    let $pageDisplay = $("#primary-show");

    const date = moment(page.editionDate).format("YYYY-MM-DD");
    renderPageNotes(page)

    let $fileAndProblemsCol = $("<div/>", {class: "col-12",id:"PDFview"});

    if (page.problems) {
        $fileAndProblemsCol.append($("<p>").text("Problems: ").append($("<pre>").text(JSON.stringify(page.problems))));
    }
    loadFrontpages(sections,$fileAndProblemsCol,page.batchid);

    let $infoDumpCol = $("<div/>", {class: "col-6"});
    $infoDumpCol.append($fileAndProblemsCol);

    let $entityInfoCol = $("<div/>", {class: "col-4 infoCol"});
    let infoHtml = `Edition titel: ${page.editionTitle}<br>`;
    infoHtml += `Section titel: ${page.sectionTitle}<br>`;
    infoHtml += `Side nummer: ${page.pageNumber}<br>`;
    infoHtml += `Enkelt side: ${page.singlePage}<br>`;
    infoHtml += `Afleverings dato: ${moment(page.deliveryDate).format("YYYY-MM-DD")}<br>`;
    infoHtml += `Udgivelses dato: ${date}<br>`;
    infoHtml += `Format type: ${page.formatType}<br>`;
    let $pageTableCol = $("<div/>",{class:"col-3"});
    let $tableRow = $("<div/>",{class:"row table-responsive"});
    let $infoRow = $("<div/>",{class:"row"});
    let $viewFrontPages = $("<div/>",{class:"row"});
    $viewFrontPages.append($("<button/>",{type:"button",class:"btn btn-secondary frontPagesBtn",text:"View frontpages",click:function (){loadFrontpages(sections,$fileAndProblemsCol,page.batchid)}}))
    $pageTableCol.append($viewFrontPages);
    $pageTableCol.append($tableRow);
    $pageTableCol.append($infoRow);
    $entityInfoCol.html(infoHtml);
    $pageDisplay.append($infoDumpCol);
    $pageDisplay.append($pageTableCol);
    createPageTable(sections,$tableRow,page.batchid,newspaperDay);
    $infoRow.append($entityInfoCol);
}

function createPageTable(sections,pageCol,batchid,newspaperDay){
    let $table = $("<table/>",{"class":"table table-striped table-hover","id":"pageNumberTable"});
    let $tableHead = $("<thead/>");
    let $tableBody = $("<tbody/>");
    let $tableRow = $("<tr/>");
    let $tableHeadPageNumber = $("<th/>",{"text":"Page Number"});
    let $tableHeadSectionTitle = $("<th/>",{"text":"Section Title"});

    $tableRow.append($tableHeadPageNumber)
    $tableRow.append($tableHeadSectionTitle)
    $tableHead.append($tableRow);
    $table.append($tableHead);
    $table.append($tableBody);
    pageCol.append($table);

    let totalPages = 0
    for (let i = 0; i < sections.length; i++) {
        for (let j = 0; j < sections[i].pages.length; j++) {
            let $dataTableRow = $("<tr/>",{"value":[j,i]});
            $dataTableRow.css("border-right","0.5em solid "+determineColor(sections[i].pages[j],sections[i].pages[j].notesCount).backgroundColor)
            $dataTableRow.append($("<td/>",{"text":sections[i].pages[j].pageNumber + " af " + sections[i].pages.length}))
            $dataTableRow.append($("<td/>",{"text":sections[i].pages[j].sectionTitle}))
            $tableBody.append($dataTableRow)
            $dataTableRow.click(function(){
                $("#tableRowHighlight").attr("id","");
                event.target.parentNode.id="tableRowHighlight";
                loadNewspaperDay(newspaperDay.batch.batchid,newspaperDay.batch.avisid,newspaperDay.date,renderNotes,0,i,j);
                //renderNotes(newspaperDay,0,i,j)
                loadImage(sections[i].pages[j],$("#PDFview"),batchid)
                })
            totalPages++;
        }
    }
    let $tableFooter = $("<tfoot/>").append($("<tr/>").append($("<td/>",{"colspan":"2","text":"Total pages: "+totalPages})));
    $table.append($tableFooter);
}

/**
 * Creates the note display with a given note.
 * puts the note into a form, which has a delete eventhandler.
 * @param {string} batchid
 * @param {Note} note
 * @returns {jQuery|HTMLElement}
 */
function createDisplayNoteForm(batchid, note) {
    let $pageForm = $("<form>", {action: "", method: "delete"});
    $pageForm.append($("<input/>", {type: "hidden", name: "batch", value: batchid}));
    $pageForm.append($("<input/>", {type: "hidden", name: "id", value: note.id}));

    const formRow = $("<div>", {id: `noteRow${note.id}`, class: "form-row"});
    $pageForm.append(formRow);

    let $pageNote = $("<span/>", {
        class: "userNotes",
        type: "text",
        name: "notes",
        text: note.note,
        readOnly: "true",
        disabled: true
    });
    formRow.append($("<label/>", {
        for: $pageNote.uniqueId().attr("id"),
        text: `-${note.username} ${moment(note.created).format("DD/MM/YYYY HH:mm:ss")}`
    }));
    formRow.append($pageNote);
    formRow.append($("<button/>", {class: "bi bi-x-circle-fill", type: "submit"}).css({
        "border": "none",
        "background-color": "transparent"
    }));
    $pageForm.submit(noteDeleteHandler);
    return $pageForm;
}

/**
 *
 * @param {Object} filename
 * @param {jQuery} element
 * @returns {*}
 */
function loadImage(filename, element, batchid) {
    element.empty();
    let result = $("<div>",{id:"frontPage"});
    element.append(result);
    filename = filename.origRelpath.replace("#", "%23");
    const url = "api/file/?file=" + filename + "&batchid=" + batchid;
    return $.ajax({type:"GET",url:url,xhrFields: {responseType: 'arraybuffer'},success: async function(data){
            let blob=new Blob([data], {type:"application/pdf"});
            let pdfurl = window.URL.createObjectURL(blob);
            result.append($("<iframe>",{src:pdfurl,type:"application/pdf",width:"100%", height:"150%"}))
    }})
}
function loadFrontpages(sections, element, batchid) {
    element.empty();
    $("#sectionRow").empty();
    $("#pageRow").empty();
    $("#tableRowHighlight").attr("id","");
    let $zoomedFrontpage = $("<div/>",{"id":"zoomedFrontpage"})
    element.append($zoomedFrontpage)
    for (let i = 0; i < sections.length; i++) {
        let sectionFrontpage = sections[i].pages[0];
        let result = $("<div>",{class:"pdfFrontPages"});
        element.append(result);
        sectionFrontpage = sectionFrontpage.origRelpath.replace("#", "%23");
        const url = "api/file/?file=" + sectionFrontpage + "&batchid=" + batchid;
        $.ajax({type:"GET",url:url,xhrFields: {responseType: 'arraybuffer'},success: async function(data){
                let blob=new Blob([data], {type:"application/pdf"});
                let pdfurl = window.URL.createObjectURL(blob);
                let $pdf =$("<iframe>",{src:pdfurl,type:"application/pdf",width:"100%", height:"100%","class":"pdfFrontpage"});
                result.append($pdf)
            }})
    }

}

/**
 *
 * @param { NewspaperSection } entity
 * @param {number} pageIndex
 */
function renderSection(entity, pageIndex,sections,newspaperDay) {
    let pages = entity.pages;

    if (pageIndex >= 0 && pageIndex < pages.length) {
        renderSinglePage(pages[pageIndex],sections,newspaperDay);
    } else {
        let $pageDisplay = $("#primary-show");
        $pageDisplay.text(`Page ${pageIndex + 1} not found. Edition only has ${pages.length} pages`);
    }
}

/**
 * @param {string} origHash
 * @param {string} newEntityIndex
 * @returns {string}
 */
function editEntityIndexInHash(origHash, newEntityIndex) {
    let hashParts = origHash.split("/");
    hashParts[hashParts.length - 4] = newEntityIndex;
    //Reset to section 0 and page 0, to ensure that we do not try to open a page/section that does not exist
    return editPageIndexInHash(editSectionIndexInHash(hashParts.join("/"), 0), 0);
}

/**
 * @param {string} origHash
 * @param {string} newSectionIndex
 * @returns {string}
 */
function editSectionIndexInHash(origHash, newSectionIndex) {
    let hashParts = origHash.split("/");
    hashParts[hashParts.length - 3] = newSectionIndex;
    //Reset to page 0, to ensure that we do not try to open a page that does not exist in this section
    return editPageIndexInHash(hashParts.join("/"), 0);
}

/**
 * @param {string} origHash
 * @param {string} newPageIndex
 * @returns {string}
 */
function editPageIndexInHash(origHash, newPageIndex) {
    let hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newPageIndex;
    return hashParts.join("/");
}



