let editionJsonData;

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
    let day = moment(date).format('YYYY-MM-DD');
    let nextDay = moment(date).add(1, 'd').format("YYYY-MM-DD")
    let pastDay = moment(date).subtract(1, 'd').format("YYYY-MM-DD")
    let url = `api/batch/${batchID}/${avisID}/${day}`
    $("#notice-div").empty();
    $("#state-div").empty();
    $("#batchOverview-table").empty();
    const $headline = $("#headline-div").empty();
    $("#primary-show").empty();
    $.getJSON("api/config.json").done(function (data) {
        editionJsonData = data.edition
    })

    $.getJSON(url)
        .done(
            /**
             * @param {NewspaperDay} newspaperDay
             * */
            function (newspaperDay) {

                $headline.append($("<a/>", {
                    class: "btn btn-secondary",
                    text: "Back to newspaper year",
                    href: `#/newspaper/${avisID}/${day.split('-')[0]}/`
                }))
                $headline.append($("<a/>", {
                    class: "btn btn-secondary", text: "Back to batch", href: `#/batch/${batchID}/`
                }))
                let $buttonForward = $("<a/>", {
                    class: "btn btn-secondary bi bi-caret-right",
                    href: `#/newspapers/${batchID}/${avisID}/${nextDay}/0/0/0/`
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
                    href: `#/newspapers/${batchID}/${avisID}/${pastDay}/0/0/0/`
                }).css({"float": "right"})
                if (day === newspaperDay.batch.startDate) {
                    $buttonBack.css({
                        "background-color": "#6c757d9e",
                        "border-color": "#6c757d9e",
                        "pointer-events": "none"
                    })
                }
                $headline.append($buttonBack)
                $headline.append($("<h1>").text(`Editions for ${avisID} on ${day}`));
                renderDayDisplay(newspaperDay, editionIndex, sectionIndex, pageIndex);
            })
        .fail(function (jqxhr, textStatus, error) {
            $headline.append($("<h1/>").text(`${jqxhr.responseText}`));
        });
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
            location.reload();
            //event.target.parentNode.append(createDisplayNoteForm(batchID,data))
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
    const $contentRow = $("<div/>", {class: "row", id: "contentRow"});
    const $dayCol = $("<div/>", {id: "dayCol", class: "col"});
    const $editionCol = $("<div/>", {id: "editionCol", class: "col"});
    const $pageCol = $("<div/>", {id: "pageCol", class: "col"});
    let $pageNavBtnToolbar = $("<div/>", {class: "btn-toolbar mb-2 mb-md-0"});
    let $pageNav = $("<div/>", {class: "btn-group mr-2 d-flex justify-content-evenly flex-wrap", id: "page-nav"});
    $contentRow.append($dayCol);
    $contentRow.append($editionCol);
    $pageNavBtnToolbar.append($pageNav);
    $pageCol.append($pageNavBtnToolbar);
    $contentRow.append($pageCol);
    $primary.append($contentRow);
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
    // if (editionIndex < 0 || editionIndex >= editions.length) {
    //     $("#primary-show").text(`Edition ${editionIndex + 1} not found. Day only has ${editions.length} editions`);
    // }
    const edition = editions[editionIndex];
    let $hiddenTextAreaValue = $("<input/>", {type: "hidden", name: "notes"});
    const $dayCol = $("#dayCol");
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
    $dayCol.append($dayNotesForm);

    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < newspaperDay.notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(newspaperDay.batch.batchid, newspaperDay.notes[i]));
    }
    $dayCol.append($noteContainer);
    if (editionIndex < 0 || editionIndex >= editions.length) {
        return;
    }
    const $editionCol = $("#editionCol");

    const $editionNav = $("<div/>", {class: 'btn-toolbar mb-2 mb-md-0'})
        .append($("<div/>", {
            class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap', id: 'edition-nav'
        }));
    $editionCol.append($editionNav);

    const editionShow = $("<div/>", {id: 'edition-show'}).append($("<h1>", {text: "show me a newspaper"}));
    $editionCol.append(editionShow);

    for (let i = 0; i < editions.length; i++) {
        const edition = editions[i];
        const link = $("<a/>").attr({
            href: editEntityIndexInHash(location.hash, i),
            class: `btn btn-sm btn-outline-secondary ${(i === editionIndex ? "active" : "")}`,
        }).text(edition.edition);
        $editionNav.append(link);
    }
    $("#edition-show").load("editionDisplay.html", function () {
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
        $editionCol.append($editionNotesForm);
        let $noteContainer = $("<div/>", {class: "noteContainer"});
        for (let i = 0; i < edition.notes.length; i++) {
            $noteContainer.append(createDisplayNoteForm(edition.batchid, edition.notes[i]));
        }
        $editionCol.append($noteContainer);
        renderSections(edition, sectionIndex);
        if (edition.sections[sectionIndex].pages.length === 1) {
            renderSinglePage(edition.section[sectionIndex].pages[0]);
        } else {
            renderSection(edition.sections[sectionIndex], pageIndex);
        }

    });
}

/**
 * Creates the section column
 * @param {NewspaperEdition} edition
 * @param {Number} sectionIndex
 */
function renderSections(edition, sectionIndex) {
    let $pageDisplay = $("#contentRow");
    let $sectionCol = $("<div/>", {id: "sectionCol", class: "col"});
    $pageDisplay.append($sectionCol);

    let sections = edition.sections.sort((a, b) => {
        return a.section < b.section ? -1 : 1
    });

    for (let i = 0; i < sections.length; i++) {
        $sectionCol.append($("<a>", {
            class: `btn btn-sm btn-outline-secondary ${i === sectionIndex ? "active" : ""}`,
            href: editSectionIndexInHash(location.hash, i),
            text: `section ${i + 1}`,
            title: sections[i].section
        }));
    }
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
    $sectionCol.append($sectionNotesForm);
    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < sections[sectionIndex].notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(edition.batchid, sections[sectionIndex].notes[i]));
    }
    $sectionCol.append($noteContainer)
}

/**
 * Creates the page column
 * @param {NewspaperPage} page
 */
function renderSinglePage(page) {
    let $pageDisplay = $("#primary-show");

    const date = moment(page.editionDate).format("YYYY-MM-DD");
    const $pageCol = $("#pageCol");

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
    $pageCol.append($pageNotesForm);
    let $noteContainer = $("<div/>", {class: "noteContainer"});
    for (let i = 0; i < page.notes.length; i++) {
        $noteContainer.append(createDisplayNoteForm(page.batchid, page.notes[i]));
    }
    $pageCol.append($noteContainer);

    let $contentRow = $("#contentRow");
    $contentRow.append($pageCol);

    let $fileAndProblemsCol = $("<div/>", {class: "col-8"});

    if (page.problems) {
        $fileAndProblemsCol.append($("<p>").text("Problems: ").append($("<pre>").text(JSON.stringify(JSON.parse(page.problems), ['type', 'filereference', 'description'], 2))));
    }
    loadImage(page.origRelpath, $fileAndProblemsCol, page.batchid);

    let $infoDumpRow = $("<div/>", {class: "row"});
    $infoDumpRow.append($fileAndProblemsCol);

    let $entityInfoCol = $("<div/>", {class: "col-4"});
    let infoHtml = `Edition titel: ${page.editionTitle}<br>`;
    infoHtml += `Section titel: ${page.sectionTitle}<br>`;
    infoHtml += `Side nummer: ${page.pageNumber}<br>`;
    infoHtml += `Enkelt side: ${page.singlePage}<br>`;
    infoHtml += `Afleverings dato: ${moment(page.deliveryDate).format("YYYY-MM-DD")}<br>`;
    infoHtml += `Udgivelses dato: ${date}<br>`;
    infoHtml += `Format type: ${page.formatType}<br>`;

    $entityInfoCol.html(infoHtml);
    $infoDumpRow.append($entityInfoCol);
    $pageDisplay.append($infoDumpRow);


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
        "background-color": "#fff"
    }));
    $pageForm.submit(noteDeleteHandler);
    return $pageForm;
}

/**
 *
 * @param {string} filename
 * @param {jQuery} element
 * @returns {*}
 */
function loadImage(filename, element, batchid) {
    let result = $("<div>");
    element.append(result);
    filename = filename.replace("#", "%23");
    const url = "api/file/?file=" + filename + "&batchid=" + batchid;
    return $.ajax({
        type: "GET", url: url, xhrFields: {responseType: 'arraybuffer'}, beforeSend: function () {
            result.text(`Loading Page`);
        }, success: async function (data) {

            let loading = pdfjsLib.getDocument(data);

            const pdf = await loading.promise;
            const page = await pdf.getPage(1);
            const scale = 1;
            const viewport = page.getViewport({scale});
            const canvas = $("<canvas/>");

            const context = canvas[0].getContext("2d");
            canvas[0].height = viewport.height;
            canvas[0].width = viewport.width;
            const renderContext = {
                canvasContext: context,
                viewport
            }

            page.render(renderContext);
            result.text("");
            result.append(canvas);
        }

    });

}

/**
 *
 * @param { NewspaperSection } entity
 * @param {number} pageIndex
 */
function renderSection(entity, pageIndex) {
    let $pageNav = $("#page-nav");
    let pages = entity.pages;
    createPageButtons(pages, $pageNav, pageIndex);

    if (pageIndex >= 0 && pageIndex < pages.length) {
        renderSinglePage(pages[pageIndex]);
    } else {
        let $pageDisplay = $("#primary-show");
        $pageDisplay.text(`Page ${pageIndex + 1} not found. Edition only has ${pages.length} pages`);
    }
}

/**
 * Made to handle if an edition contains too many pages.
 * Creates the buttons to switch between the pages on an edition.
 * @param {NewspaperPage[]} pages
 * @param {jQuery|HTMLElement} parent
 * @param {number} page
 */
function createPageButtons(pages, parent, page) {
    let active;
    let cutLow = page - 1;
    let cutHigh = page + 1;
    parent.append($("<a/>", {
        class: "btn btn-sm btn-outline-secondary bi bi-arrow-left nextAndPreviousPage",
        href: page === 0 ? editPageIndexInHash(location.hash, page) : editPageIndexInHash(location.hash, page - 1)
    }));

    if (pages.length - 1 < 8) {
        for (let p = 0; p < pages.length; p++) {
            active = page === p ? "active" : "no";
            const link = $("<a/>").attr({
                href: editPageIndexInHash(location.hash, p),

                class: `btn btn-sm btn-outline-secondary ${active}`
            }).text(p + 1);
            determineColor(pages[p], link, pages[p].notes.length);
            parent.append(link);
        }
    } else {
        let link = $("<a/>", {
            class: `btn btn-sm btn-outline-secondary ${page === 0 ? "active" : ""}`,
            href: editPageIndexInHash(location.hash, 0)
        }).text(1);

        determineColor(pages[0], link, pages[0].notes.length);

        parent.append(link);
        if (page > 2) {
            parent.append($("<a/>", {
                class: "btn btn-sm out-of-range",
                href: editPageIndexInHash(location.hash, page - 2)
            }).text("..."));
        }

        if (page === 0) {
            cutHigh = page + 2;
        }
        if (page === pages.length - 1) {
            cutLow = page - 2;
        }

        for (let p = cutLow < 0 ? 0 : cutLow; p <= cutHigh; p++) {
            if (p !== 0 && p < pages.length - 1) {
                active = page === p ? "active" : "no";
                const link = $("<a/>").attr({
                    href: editPageIndexInHash(location.hash, p),

                    class: `btn btn-sm btn-outline-secondary ${active}`
                }).text(p + 1);
                determineColor(pages[p], link, pages[p].notes.length);

                parent.append(link);
            }
        }
        if (page < pages.length - 2) {
            if (page < pages.length - 3) {
                parent.append($("<a/>", {
                    class: "btn btn-sm out-of-range",
                    href: editPageIndexInHash(location.hash, page + 2)
                }).text("..."));
            }

        }
        link = $("<a/>", {
            class: `btn btn-sm btn-outline-secondary ${page === pages.length - 1 ? "active" : ""}`,
            href: editPageIndexInHash(location.hash, pages.length - 1)
        }).text(pages.length);
        determineColor(pages[pages.length - 1], link, pages[pages.length - 1].notes.length);
        parent.append(link);
    }
    parent.append($("<a/>", {
        class: "btn btn-sm btn-outline-secondary bi bi-arrow-right nextAndPreviousPage",
        href: page === pages.length - 1 ? editPageIndexInHash(location.hash, page) : editPageIndexInHash(location.hash, page + 1)
    }));
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



