document.addEventListener("DOMContentLoaded", function () {

    const customerWorkbook =
        document.getElementById("customerWorkbook");

    const fetchButton =
        document.getElementById("fetchButton");

    const fetchAndVerifyButton =
        document.getElementById("fetchAndVerifyButton");

    const progressMessage =
        document.getElementById("progressMessage");

    const progressText =
        document.getElementById("progressText");

    const successMessage =
        document.getElementById("successMessage");

    const errorMessage =
        document.getElementById("errorMessage");

    const buildIdElement =
        document.getElementById("buildId");

    const resultMessageElement =
        document.getElementById("resultMessage");

    const errorTextElement =
        document.getElementById("errorText");

    customerWorkbook.addEventListener(
        "change",
        handleWorkbookSelection
    );

    fetchButton.addEventListener(
        "click",
        handleFetch
    );

    fetchAndVerifyButton.addEventListener(
        "click",
        handleFetchAndVerify
    );

    function handleWorkbookSelection() {
        clearMessages();

        const selectedFile =
            customerWorkbook.files[0];

        const validWorkbook =
            selectedFile !== undefined
            && selectedFile.name
                .toLowerCase()
                .endsWith(".xlsx");

        fetchButton.disabled = !validWorkbook;
        fetchAndVerifyButton.disabled = !validWorkbook;

        if (selectedFile && !validWorkbook) {
            showError("Only .xlsx files are supported.");
        }
    }

    async function handleFetch() {
        const selectedFile =
            customerWorkbook.files[0];

        if (!selectedFile) {
            showError("Please select a customer Excel file.");
            return;
        }

        clearMessages();
        setOperationInProgress(
            true,
            "Fetch operation is running. Please wait."
        );

        const formData = new FormData();

        formData.append(
            "customerWorkbook",
            selectedFile
        );

        try {
            const response = await fetch(
                "/api/fetch",
                {
                    method: "POST",
                    body: formData
                }
            );

            if (!response.ok) {
                const errorResponse =
                    await readErrorResponse(response);

                throw new Error(
                    errorResponse.message
                    || "Fetch operation failed."
                );
            }

            const buildId =
                response.headers.get("X-Build-Id");

            const zipContent =
                await response.blob();

            const zipFileName =
                (buildId || "fetch-results") + ".zip";

            downloadZip(
                zipContent,
                zipFileName
            );

            showSuccess(
                buildId,
                "Responses saved and downloaded as a ZIP file."
            );

        } catch (error) {
            showError(error.message);

        } finally {
            setOperationInProgress(false);
        }
    }

    function handleFetchAndVerify() {
        clearMessages();

        showError(
            "Fetch and Verify is not implemented yet."
        );
    }

    async function readErrorResponse(response) {
        const responseText =
            await response.text();

        if (!responseText) {
            return {};
        }

        try {
            return JSON.parse(responseText);

        } catch (error) {
            return {
                message: responseText
            };
        }
    }

    function downloadZip(
        zipContent,
        zipFileName
    ) {
        const downloadUrl =
            URL.createObjectURL(zipContent);

        const downloadLink =
            document.createElement("a");

        downloadLink.href = downloadUrl;
        downloadLink.download = zipFileName;

        document.body.appendChild(downloadLink);
        downloadLink.click();
        downloadLink.remove();

        URL.revokeObjectURL(downloadUrl);
    }

    function setOperationInProgress(
        operationInProgress,
        message
    ) {
        customerWorkbook.disabled =
            operationInProgress;

        fetchButton.disabled =
            operationInProgress;

        fetchAndVerifyButton.disabled =
            operationInProgress;

        if (operationInProgress) {
            progressText.textContent =
                message;

            progressMessage.classList.remove(
                "hidden"
            );

            return;
        }

        progressMessage.classList.add(
            "hidden"
        );

        handleWorkbookSelection();
    }

    function showSuccess(
        buildId,
        resultMessage
    ) {
        buildIdElement.textContent =
            buildId || "";

        resultMessageElement.textContent =
            resultMessage;

        successMessage.classList.remove(
            "hidden"
        );
    }

    function showError(message) {
        errorTextElement.textContent =
            message
            || "An unexpected error occurred.";

        errorMessage.classList.remove(
            "hidden"
        );
    }

    function clearMessages() {
        progressMessage.classList.add(
            "hidden"
        );

        successMessage.classList.add(
            "hidden"
        );

        errorMessage.classList.add(
            "hidden"
        );

        buildIdElement.textContent = "";
        resultMessageElement.textContent = "";
        errorTextElement.textContent = "";
    }
});