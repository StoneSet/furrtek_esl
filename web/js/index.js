(function() {

  function keyDownHandler(e) {
    const audio = document.querySelector(`audio[data-key="${e.key}"]`);
    const key = document.querySelector(`div[data-key="${e.key}"]`);
    playSound(audio, key);
  }

  function clickHandler(e) {
    if (this.dataset.key) {
      const audio = document.querySelector(`audio[data-key="${this.dataset.key}"]`);
      const key = document.querySelector(`div[data-key="${this.dataset.key}"]`);
      playSound(audio, key);
    } else { // no keyboard shortcut
      const audio = document.querySelector(`audio#${this.dataset.target}`);
      const key = document.querySelector(`div[data-target="${this.dataset.target}"]`);
      playSound(audio, key);
    }

  }

  function playSound(audio, key) {
    if (!audio) return;
    key.classList.add('playing');
    audio.currentTime = 0;
    audio.play();
  }

  function removeTransition(e) {
    e.target.classList.remove('playing');
  }

  const keys = Array.from(document.querySelectorAll('.list__item'));
  keys.forEach(key => key.addEventListener('transitionend', removeTransition));
  window.addEventListener('keydown', keyDownHandler);

  keys.forEach(key => key.addEventListener('click', clickHandler));

})();
