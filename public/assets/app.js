(() => {
  const flash = document.querySelector('.flash');
  if (flash) {
    window.setTimeout(() => {
      flash.style.opacity = '0';
      flash.style.transition = 'opacity .3s ease';
      window.setTimeout(() => flash.remove(), 350);
    }, 5000);
  }
})();
