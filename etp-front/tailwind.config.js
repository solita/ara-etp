module.exports = {
  theme: {
    colors: {
      primary: '#538000',
      secondary: '#2e5053',
      tertiary: '#f1efe4',
      dark: '#000',
      light: '#fff',
      primarydark: '#345000',
      secondarydark: '#343841',
      link: '#538000',
      hover: '#538000',
      althover: '#c5e191',
      focus: '#538000',
      active: '#6d6d6d',
      disabled: '#a7a7a7',
      lighterdisabled: '#f1efe4',
      error: '#9e0000',
      warning: '#ead049',
      success: '#538000',
      background: '#eee',
      backgroundhalf: 'rgba(238, 238, 238, 0.5)',
      hr: 'rgba(52, 56, 65, 0.3)',
      transparent: 'rgba(0,0,0,0)',
      tableborder: 'rgba(167,167,167,0.3)',
      beige: '#f2f8e7',
      // These are from official ARA style guide. When doing new work, check if you can use these
      'ara-2021-green': '#94c43a', // Ara basic green color. Not necessarily accessible
      'ara-2021-green-background': '#e5efcd', // Basic background green when not using white
      'ara-2021-green-large-text': '#79a130', // Accessible when used for large texts 18pt, 14pt bold
      'ara-2021-green-small-text': '#59771e', // Accessible even with smaller text. Can be used as colored background for small white text
      'ara-2021-light-gray-background': '#f1f1f1', // One of the basic background colors
      'ara-2021-basic-gray': '#686767' // Basic gray color. Accessible when paired with white. Same as the disabled color
    },
    extend: {
      fontFamily: {
        display: ['Montserrat', 'sans-serif'],
        body: ['Montserrat', 'sans-serif'],
        icon: 'Material Icons',
        'icon-outlined': 'Material Icons Outlined'
      },
      borderWidth: {
        1: '1px',
        3: '3px',
        7: '7px'
      },
      inset: {
        100: '100%',
        '3em': '3em'
      },
      opacity: {
        15: '0.15'
      },
      boxShadow: {
        'hover-2-primary': '0 0.5em #538000',
        'hover-2-secondary': '0 0.5em #2e5053',
        dropdownlist: '0 25px 50px 0px rgba(0, 0, 0, 0.25)'
      },
      maxWidth: {
        '7em': '7em',
        70: '70%',
        1280: '1280px',
        1440: '1440px'
      },
      minWidth: {
        10: '10em'
      },
      width: {
        sm: '640px',
        md: '768px',
        lg: '1024px',
        xl: '1280px'
      },
      minHeight: {
        85: '85vh',
        '2.5em': '2.5em',
        '3em': '3em'
      },
      letterSpacing: {
        xl: '0.15em'
      }
    },
    fontSize: {
      // Tailwind V1 theming. Feel free to change if you are a visual designer
      xs: '0.75rem',
      sm: '0.875rem',
      base: '1rem',
      lg: '1.125rem',
      xl: '1.25rem',
      '2xl': '1.5rem',
      '3xl': '1.875rem',
      '4xl': '2.25rem',
      '5xl': '3rem',
      '6xl': '4rem'
    }
  },
  plugins: [],
  content: ['./src/**/*.svelte']
};
