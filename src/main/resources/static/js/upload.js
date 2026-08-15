(() => {
    Dropzone.autoDiscover = false;

    const form = document.getElementById('media-dropzone');
    const messages = form.dataset;
    const resultDialog = document.getElementById('upload-result');
    const resultTitle = document.getElementById('result-title');
    const resultMessage = document.getElementById('result-message');
    const failedFiles = document.getElementById('failed-files');
    const failedFileList = document.getElementById('failed-file-list');
    const okButton = document.getElementById('result-ok');
    const confetti = document.getElementById('confetti');
    let resultVisible = false;

    const format = (template, value) => template.replace('{0}', value);

    const clearConfetti = () => confetti.replaceChildren();

    const celebrate = () => {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            return;
        }

        const fragment = document.createDocumentFragment();
        for (let index = 0; index < 100; index++) {
            const piece = document.createElement('span');
            piece.style.setProperty('--x', `${Math.random() * 100}vw`);
            piece.style.setProperty('--delay', `${Math.random() * 0.8}s`);
            piece.style.setProperty('--duration', `${2.4 + Math.random() * 2}s`);
            piece.style.setProperty('--drift', `${(Math.random() - 0.5) * 22}vw`);
            piece.style.setProperty('--rotation', `${360 + Math.random() * 720}deg`);
            piece.style.setProperty('--color', `hsl(${Math.random() * 360} 85% 65%)`);
            fragment.appendChild(piece);
        }
        confetti.appendChild(fragment);
    };

    const openResult = dropzone => {
        const uploaded = dropzone.files.filter(file => file.status === Dropzone.SUCCESS);
        const failed = dropzone.files.filter(file => file.status !== Dropzone.SUCCESS);
        const successful = failed.length === 0;

        resultVisible = true;
        resultDialog.classList.toggle('is-success', successful);
        resultDialog.classList.toggle('is-error', !successful);
        resultTitle.textContent = successful ? messages.resultSuccessTitle : messages.resultErrorTitle;
        failedFiles.hidden = successful;
        failedFileList.replaceChildren();

        if (successful) {
            resultMessage.textContent = uploaded.length === 1
                ? messages.resultSuccessOne
                : format(messages.resultSuccessMany, uploaded.length);
            celebrate();
        } else {
            const summary = uploaded.length === 0
                ? messages.resultErrorNone
                : uploaded.length === 1
                    ? messages.resultErrorOne
                    : format(messages.resultErrorMany, uploaded.length);
            resultMessage.textContent = summary;
            failed.forEach(file => {
                const item = document.createElement('li');
                item.textContent = file.name;
                failedFileList.appendChild(item);
            });
        }

        resultDialog.showModal();
    };

    const dropzone = new Dropzone(form, {
        url: form.action,
        method: 'post',
        paramName: 'files',
        acceptedFiles: 'image/*,video/*',
        autoProcessQueue: true,
        uploadMultiple: false,
        parallelUploads: 3,
        maxFilesize: null,
        timeout: 0,
        dictInvalidFileType: messages.invalidType,

        init() {
            this.on('success', (file, response) => {
                file.previewElement.setAttribute('aria-label', response?.message || messages.completed);
            });

            this.on('error', (file, response, xhr) => {
                const message = xhr?.status === 403
                    ? messages.forbidden
                    : response?.message || (typeof response === 'string' ? response : messages.failed);
                const errorMessage = file.previewElement?.querySelector('[data-dz-errormessage]');
                if (errorMessage) {
                    errorMessage.textContent = message;
                }
                file.previewElement?.setAttribute('aria-label', message);
            });

            this.on('queuecomplete', () => {
                const uploadStillRunning = this.files.some(file =>
                    [Dropzone.ADDED, Dropzone.QUEUED, Dropzone.UPLOADING].includes(file.status));
                if (this.files.length > 0 && !uploadStillRunning && !resultVisible) {
                    openResult(this);
                }
            });
        }
    });

    resultDialog.addEventListener('cancel', event => event.preventDefault());
    okButton.addEventListener('click', () => {
        resultDialog.close();
        clearConfetti();
        dropzone.removeAllFiles(true);
        form.reset();
        resultVisible = false;
    });
})();
