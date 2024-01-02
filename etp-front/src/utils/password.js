const passwordCharacters =
  'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-.!';

export const generatePassword = () => {
  let password = '';

  for (let n = 0; n < 32; ++n) {
    password +=
      passwordCharacters[Math.floor(Math.random() * passwordCharacters.length)];
  }

  return password;
};
