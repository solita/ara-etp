const FIXTURES = {
  headers: {
    'x-amzn-oidc-accesstoken':
      'eyJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJwYWFrYXl0dGFqYUBzb2xpdGEuZmkiLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCIsImF1dGhfdGltZSI6MTU4MzIzMDk2OSwiaXNzIjoiaHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL3NvbGl0YS9ldHAtY29yZS9mZWF0dXJlL0FFLTQzLWF1dGgtaGVhZGVycy1oYW5kbGluZy9ldHAtYmFja2VuZC9zcmMvbWFpbi9yZXNvdXJjZXMiLCJleHAiOjE4OTM0NTYwMDAsImlhdCI6MTU4MzQxMzQyNCwidmVyc2lvbiI6MiwianRpIjoiNWZkZDdhMjktN2VlYS00ZjNkLWE3YTYtYzIyODQyNmY2MTJiIiwiY2xpZW50X2lkIjoidGVzdC1jbGllbnRfaWQiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUifQ.PY5_jWcdxhCyn2EpFpss7Q0R3_xH1PvHi4mxDLorpppHnciGT2kFLeutebi7XeLtTYwmttTxxg2tyUyX0_UF7zj_P-tdq-kZQlud1ENmRaUxLXO5mTFKXD7zPb6BPFNe0ewRQ7Uuv3lDk_IxOf-6i86VDYB8luyesEXq7ra4S4l8akFodW_QYBSZQnUva_CVyzsTNcmgGTyrz2NI6seT1x6Pt1uFdYI97FHKlCCWVL1Z042omfujfta8j8XkTWdhKf3dfsHRWjrw31xqOkgD7uwPKcrC0U-wIj3U0uX0Rz2Tk4T-kIq4XTkKttYpkJqOmMFAYuhk6MDjfRkPWBZhUA',
    'x-amzn-oidc-identity': 'paakayttaja@solita.fi',
    'x-amzn-oidc-data':
      'eyJ0eXAiOiJKV1QiLCJraWQiOiJ0ZXN0LWtpZCIsImFsZyI6IlJTMjU2IiwiaXNzIjoidGVzdC1pc3MiLCJjbGllbnQiOiJ0ZXN0LWNsaWVudCIsInNpZ25lciI6InRlc3Qtc2lnbmVyIiwiZXhwIjoxODkzNDU2MDAwfQ.eyJzdWIiOiJwYWFrYXl0dGFqYUBzb2xpdGEuZmkiLCJjdXN0b206VklSVFVfbG9jYWxJRCI6InZ2aXJrYW1pZXMiLCJjdXN0b206VklSVFVfbG9jYWxPcmciOiJ0ZXN0aXZpcmFzdG8uZmkiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6InRlc3QtaXNzIn0.BfuDVOFUReiJd6N05Re6affps_47AA0F5o-g6prmXgAnk4lB1S3k9RpovCFU3-R5Zn0p38QTiwi5dENHCHaj1A6MGHHKeYd7vBZK0VquuBxlIQH-4k1MWLvpYnkK3yuEvfmbRb3jYspCA_4N-AF21cCyjd15RiuIawLCEM0Km1DRgLhXIBta6XCGSRwaRmrT7boDRMp7hUkYPpoakCahMC70sjyuvLE0pjAy1_S09g4SkboentI7WhfsfN4uAHbKy6ViVMfsnwVVvKsM8dXav_a-6PoNGywuUbi8nHt8c20KiB_AzAEYSqxbRX1YBd0UHlYS16LbLtMBTOctCBLDMg'
  }
};

const openToimenpideCreationAndCheckAllowedToimenpidetypes = (
  toimenpideToCreate,
  allowedToimenpidetypes
) => {
  cy.get('[data-cy="toimenpide-type-selection"]')
    .click()
    .parent()
    .within(() => {
      // Number of available options should be the count of allowed toimenpidetypes and the default 'Ei valintaa' option
      cy.get('li').should('have.length', allowedToimenpidetypes.length + 1);

      allowedToimenpidetypes.map(toimenpidetype => {
        cy.contains(toimenpidetype);
      });
      cy.contains(toimenpideToCreate).click();
    });
};

const submitToimenpide = () => {
  cy.get('[data-cy="submit-button"]').click();
};

const checkToimenpideCreationSucceeded = () => {
  cy.get('.alert').contains('Toimenpide on lähetetty.');
  // Wait until the notification disappears before continuing
  cy.get('.alert').should('not.exist');
};

const toimenpideIsInCreatedToimenpideList = toimenpide => {
  cy.contains(toimenpide);
};

const commentIsInToimenpideList = comment => {
  cy.contains(comment);
};

const createKaytonvalvonta = () => {
  // Fill in minimum information
  cy.get('[data-cy="kohde.katuosoite"]')
    .type('Testikatu 26')
    .should('have.value', 'Testikatu 26');
  cy.get('[data-cy="kohde.postinumero"]').type('90100');

  // Create the valvonta
  cy.get('[data-cy="-submit"]').click();

  // Add an omistajaosapuoli to the valvonta
  cy.get('[data-cy="Uusi henkilö"]').click();
  cy.get('[data-cy="henkilo.etunimi"]')
    .type('Enni')
    .should('have.value', 'Enni');
  cy.get('[data-cy="henkilo.sukunimi"]')
    .type('Esimerkki')
    .should('have.value', 'Esimerkki');
  cy.selectInSelect('henkilo.rooli-id', 'Omistaja');
  cy.get('[data-cy="-submit"]').click();
};

const startValvonta = () => {
  cy.get('[data-cy="start-button"]').click();
  cy.get('[data-cy="toimenpide.description"]')
    .type('Aloitetaan valvonta')
    .should('have.value', 'Aloitetaan valvonta')
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Valvonnan aloitus');
  commentIsInToimenpideList('Aloitetaan valvonta');
};

const createKehotus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes('Kehotus', [
    'Kehotus',
    'Valvonnan lopetus'
  ]);
  cy.selectInSelect('document-selector', 'Kehotus');
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Kehotus');
};

const createVaroitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes('Varoitus', [
    'Kehotus',
    'Varoitus',
    'Valvonnan lopetus'
  ]);
  cy.selectInSelect('document-selector', 'Varoitus');
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Varoitus');
};

const createKaskypaatosKuulemiskirje = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / kuulemiskirje',
    ['Kehotus', 'Varoitus', 'Käskypäätös / kuulemiskirje', 'Valvonnan lopetus']
  );
  // Fine should have default value of 800 euros
  cy.get('[data-cy="toimenpide.fine"]')
    .should('have.value', 800)
    .clear()
    .type('9000')
    .should('have.value', 9000)
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Käskypäätös / kuulemiskirje');
};

const createKaskypaatosVarsinainenPaatos = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / varsinainen päätös',
    [
      'Käskypäätös / kuulemiskirje',
      'Käskypäätös / varsinainen päätös',
      'Valvonnan lopetus'
    ]
  );

  // Fine should have a default value of 9000 euros as it was set in the previous toimenpide
  cy.get('[data-cy="toimenpide.fine"]').should('have.value', 9000);

  cy.get('[data-cy="toimenpide.department-head-title-fi"]')
    .clear()
    .type('Ylijohtaja')
    .should('have.value', 'Ylijohtaja')
    .blur();

  cy.get('[data-cy="toimenpide.department-head-title-sv"]')
    .clear()
    .type('Överdirektör')
    .should('have.value', 'Överdirektör')
    .blur();

  cy.get('[data-cy="toimenpide.department-head-name"]')
    .clear()
    .type('Jonna Johtaja')
    .should('have.value', 'Jonna Johtaja')
    .blur();

  cy.selectInSelect(
    'recipient-answered-selector-0',
    'Asianosainen antoi vastineen kuulemiskirjeeseen'
  );

  cy.get('[data-cy="toimenpide.answer-commentary-fi-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti');

  cy.get('[data-cy="toimenpide.answer-commentary-sv-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti ruotsiksi')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti ruotsiksi');

  cy.get('[data-cy="toimenpide.statement-fi-0"]')
    .type('Tähän kirjoitetaan kannanotto.')
    .should('have.value', 'Tähän kirjoitetaan kannanotto.');
  cy.get('[data-cy="toimenpide.statement-sv-0"]')
    .type('Tähän kirjoitetaan kannanotto ruotsiksi.')
    .should('have.value', 'Tähän kirjoitetaan kannanotto ruotsiksi.');

  cy.selectInSelect(
    'administrative-court-selector-0',
    'Helsingin hallinto-oikeus'
  );

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Käskypäätös / varsinainen päätös');
};

const createKaskypaatosTiedoksiantoEnsimmainenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / tiedoksianto (ensimmäinen postitus)',
    [
      'Käskypäätös / varsinainen päätös',
      'Käskypäätös / tiedoksianto (ensimmäinen postitus)',
      'Valvonnan lopetus'
    ]
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetän tämän kirjeenä nyt.')
    .should('have.value', 'Lähetän tämän kirjeenä nyt.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / tiedoksianto (ensimmäinen postitus)'
  );
  commentIsInToimenpideList('Lähetän tämän kirjeenä nyt.');
};

const createKaskypaatosTiedoksiantoToinenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / tiedoksianto (toinen postitus)',
    ['Käskypäätös / tiedoksianto (toinen postitus)', 'Valvonnan lopetus']
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetän tämän kirjeenä nyt uudestaan, kun se palautui lähettäjälle.')
    .should(
      'have.value',
      'Lähetän tämän kirjeenä nyt uudestaan, kun se palautui lähettäjälle.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / tiedoksianto (toinen postitus)'
  );
  commentIsInToimenpideList(
    'Lähetän tämän kirjeenä nyt uudestaan, kun se palautui lähettäjälle.'
  );
};

const createKaskypaatosTiedoksiantoHaastemies = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / tiedoksianto (Haastemies)',
    [
      'Käskypäätös / tiedoksianto (Haastemies)',
      'Käskypäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ]
  );

  cy.selectInSelect('karajaoikeus-selector-0', 'Ahvenanmaan käräjäoikeus');
  cy.get('[data-cy="haastemies-email-0"]')
    .type('haastemies@poliisitaikarajaoikeus.fi')
    .should('have.value', 'haastemies@poliisitaikarajaoikeus.fi')
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / tiedoksianto (Haastemies)'
  );
};

const createKaskypaatosValitusajanOdotusJaUmpeutuminen = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / valitusajan odotus ja umpeutuminen',
    ['Käskypäätös / valitusajan odotus ja umpeutuminen', 'Valvonnan lopetus']
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .should(
      'have.value',
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / valitusajan odotus ja umpeutuminen'
  );
  commentIsInToimenpideList(
    'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
  );
};

const createHaOKasittely = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes('HaO-käsittely', [
    'HaO-käsittely',
    'Sakkopäätös / kuulemiskirje',
    'Valvonnan lopetus'
  ]);

  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .should(
      'have.value',
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('HaO-käsittely');
  commentIsInToimenpideList(
    'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
  );
};

const createSakkopaatosKuulemiskirje = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / kuulemiskirje',
    [
      'Sakkopäätös / kuulemiskirje',
      'Sakkoluettelon lähetys menossa',
      'Valvonnan lopetus'
    ]
  );

  // Fine should have a default value of 9000 euros as it was set previously
  // Change it now to another amount
  cy.get('[data-cy="toimenpide.fine"]')
    .should('have.value', 9000)
    .clear()
    .type('4500')
    .should('have.value', 4500)
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Sakkopäätös / kuulemiskirje');
};

const createSakkopaatosVarsinainenPaatos = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / varsinainen päätös',
    [
      'Sakkopäätös / kuulemiskirje',
      'Sakkopäätös / varsinainen päätös',
      'Valvonnan lopetus'
    ]
  );

  // Fine is now 4500 after setting it in the previous toimenpide
  cy.get('[data-cy="toimenpide.fine"]').should('have.value', 4500);

  // Department head information is what was previously used
  cy.get('[data-cy="toimenpide.department-head-title-fi"]').should(
    'have.value',
    'Ylijohtaja'
  );

  cy.get('[data-cy="toimenpide.department-head-title-sv"]').should(
    'have.value',
    'Överdirektör'
  );

  cy.get('[data-cy="toimenpide.department-head-name"]').should(
    'have.value',
    'Jonna Johtaja'
  );

  cy.selectInSelect(
    'recipient-answered-selector-0',
    'Asianosainen antoi vastineen kuulemiskirjeeseen'
  );

  // Notice about updating statement is visible after selecting the previous option
  cy.contains('Tarkista ja muuta teksti kohdassa kannanotto vastineeseen');

  cy.get('[data-cy="toimenpide.answer-commentary-fi-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti');

  cy.get('[data-cy="toimenpide.answer-commentary-sv-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti ruotsiksi')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti ruotsiksi');

  // Statement has default text filled in
  cy.get('[data-cy="toimenpide.statement-fi-0"]').should(
    'have.value',
    'ARAn päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Maksuun tuomittavan uhkasakon määrä on sama kuin mitä se on ollut ARAn päätöksessä. ARAn näkemyksen mukaan uhkasakko tuomitaan maksuun täysimääräisenä, koska Asianosainen ei ole noudattanut päävelvoitetta lainkaan, eikä ole myöskään esittänyt noudattamatta jättämiselle pätevää syytä.'
  );
  cy.get('[data-cy="toimenpide.statement-sv-0"]').should(
    'have.value',
    'Ändring i ARAs beslut har inte sökts, dvs. beslutet har vunnit laga kraft. Vitesbeloppet som döms ut är detsamma som det var i ARAs beslut. Enligt ARAs uppfattning döms vitet ut till fullt belopp, eftersom Esimerkki inte alls har iakttagit huvudförpliktelsen och inte heller har angett någon giltig orsak till försummelsen.'
  );

  cy.selectInSelect(
    'administrative-court-selector-0',
    'Pohjois-Suomen hallinto-oikeus'
  );

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Sakkopäätös / varsinainen päätös');
};

const createSakkopaatosTiedoksiantoEnsimmainenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / tiedoksianto (ensimmäinen postitus)',
    [
      'Sakkopäätös / varsinainen päätös',
      'Sakkopäätös / tiedoksianto (ensimmäinen postitus)',
      'Valvonnan lopetus'
    ]
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetimme sakkopäätöksen ensimmäisen kerran.')
    .should('have.value', 'Lähetimme sakkopäätöksen ensimmäisen kerran.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / tiedoksianto (ensimmäinen postitus)'
  );
  commentIsInToimenpideList('Lähetimme sakkopäätöksen ensimmäisen kerran.');
};

const createSakkopaatosTiedoksiantoToinenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / tiedoksianto (toinen postitus)',
    [
      'Sakkopäätös / tiedoksianto (toinen postitus)',
      'Sakkopäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ]
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetimme sakkopäätöksen toisen kerran.')
    .should('have.value', 'Lähetimme sakkopäätöksen toisen kerran.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / tiedoksianto (toinen postitus)'
  );
};

const createSakkopaatosTiedoksiantoHaastemies = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / tiedoksianto (Haastemies)',
    [
      'Sakkopäätös / tiedoksianto (Haastemies)',
      'Sakkopäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ]
  );

  cy.selectInSelect(
    'administrative-court-selector-0',
    'Pohjois-Suomen hallinto-oikeus'
  );
  cy.selectInSelect('karajaoikeus-selector-0', 'Ahvenanmaan käräjäoikeus');
  cy.get('[data-cy="haastemies-email-0"]')
    .type('haastemies@poliisitaikarajaoikeus.fi')
    .should('have.value', 'haastemies@poliisitaikarajaoikeus.fi')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / tiedoksianto (Haastemies)'
  );
};

const createSakkopaatosValitusajanOdotusJaUmpeutuminen = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / valitusajan odotus ja umpeutuminen',
    ['Sakkopäätös / valitusajan odotus ja umpeutuminen', 'Valvonnan lopetus']
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .should(
      'have.value',
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / valitusajan odotus ja umpeutuminen'
  );
};

const createSakkopaatosHaOKasittely = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes('HaO-käsittely', [
    'HaO-käsittely',
    'Valvonnan lopetus',
    'Sakkoluettelon lähetys menossa'
  ]);

  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .should(
      'have.value',
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('HaO-käsittely');
  commentIsInToimenpideList(
    'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
  );
};

const createSakkoluettelonLahetysMenossa = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkoluettelon lähetys menossa',
    [
      'Sakkoluettelon lähetys menossa',
      'Sakkopäätös / kuulemiskirje',
      'Valvonnan lopetus'
    ]
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetimme sakkoluettelon.')
    .should('have.value', 'Lähetimme sakkoluettelon.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Sakkoluettelon lähetys menossa');
};

const closeValvonta = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes('Valvonnan lopetus', [
    'Valvonnan lopetus'
  ]);
  cy.get('[data-cy="toimenpide.description"]')
    .type('Valvonta on suoritettu loppuun.')
    .should('have.value', 'Valvonta on suoritettu loppuun.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Valvonnan lopetus');
  commentIsInToimenpideList('Valvonta on suoritettu loppuun.');
};

const reopenValvonta = () => {
  cy.get('[data-cy="toimenpide.description"]')
    .type('Laitoin vahingossa kiinni, avataan uudestaan.')
    .should('have.value', 'Laitoin vahingossa kiinni, avataan uudestaan.')
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Suljetun valvonnan uudelleenavaus');
  commentIsInToimenpideList('Laitoin vahingossa kiinni, avataan uudestaan.');
};

context('Käytönvalvonta', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
  });

  it('full process succeeds', () => {
    // Navigate to creating a new käytönvalvonta
    cy.visit('/');
    cy.contains('Käytön­valvon­nat').click();
    cy.get('[data-cy="Uusi valvonta"]').click();

    createKaytonvalvonta();

    // Navigate to the valvonta page
    cy.get('[data-cy^=KV]').click();
    cy.get('[data-cy=Valvonta]').click();

    startValvonta();

    createKehotus();

    createVaroitus();

    createKaskypaatosKuulemiskirje();

    createKaskypaatosVarsinainenPaatos();

    createKaskypaatosTiedoksiantoEnsimmainenPostitus();

    createKaskypaatosTiedoksiantoToinenPostitus();

    createKaskypaatosTiedoksiantoHaastemies();

    createKaskypaatosValitusajanOdotusJaUmpeutuminen();

    createHaOKasittely();

    createSakkopaatosKuulemiskirje();

    createSakkopaatosVarsinainenPaatos();

    createSakkopaatosTiedoksiantoEnsimmainenPostitus();

    createSakkopaatosTiedoksiantoToinenPostitus();

    createSakkopaatosTiedoksiantoHaastemies();

    createSakkopaatosValitusajanOdotusJaUmpeutuminen();

    createSakkopaatosHaOKasittely();

    createSakkoluettelonLahetysMenossa();

    closeValvonta();

    // Aloita valvonta button should not exist at this point
    cy.get('[data-cy="start-button"]').should('not.exist');

    // After valvonta has been closed, there should be a button to reopen it if it was closed mistakenly
    cy.get('[data-cy="continue-button"]').click();

    //Reopen the valvonta
    reopenValvonta();
  });
});
