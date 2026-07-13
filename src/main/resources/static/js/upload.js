(() => {
    Dropzone.autoDiscover = false;

    const form = document.getElementById('media-dropzone');
    const messages = form.dataset;

    new Dropzone(form, {
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
        }
    });
})();
